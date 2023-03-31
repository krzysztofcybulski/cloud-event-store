package me.kcybulski.ces.service.grpc

import io.grpc.BindableService
import mu.KLogging
import ratpack.core.handling.Handler
import ratpack.core.impose.Impositions
import ratpack.core.impose.UserRegistryImposition
import ratpack.core.server.RatpackServer
import ratpack.core.server.RatpackServerSpec
import ratpack.core.server.ServerConfig
import ratpack.core.server.internal.HostUtil.determineHost
import ratpack.core.server.internal.RatpackServerDefinition
import ratpack.core.server.internal.ServerCapturer
import ratpack.core.server.internal.ServerRegistry
import ratpack.core.service.internal.DefaultEvent
import ratpack.core.service.internal.ServicesGraph
import ratpack.exec.internal.DefaultExecController
import ratpack.exec.internal.ExecControllerInternal
import ratpack.exec.internal.ExecThreadBinding
import ratpack.exec.registry.Registry
import ratpack.func.Action
import ratpack.func.Exceptions
import java.time.Duration
import java.util.Optional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicBoolean
import ratpack.func.Function as RatpackFunction


class DefaultGrpcRatpackServer(
    private val definitionFactory: Action<in RatpackServerSpec>,
    private val impositions: Impositions
) : GrpcRatpackServer {

    private var runningState: RunningState? = null
    private var reloading = false
    private val needsReload = AtomicBoolean(false)
    private var shutdownHookThread: Thread? = null

    init {
        ServerCapturer.capture(this)
    }

    override fun getScheme(): String? = when {
        isRunning && isSsl() -> "https"
        isRunning && !isSsl() -> "http"
        else -> null
    }

    private fun isSsl() = runningState?.useSsl ?: false

    override fun getBindPort(): Int = runningState?.server?.port ?: -1

    override fun getBindHost(): String? = runningState?.server?.address?.let { determineHost(it) }

    override fun isRunning(): Boolean = runningState?.server?.isRunning ?: false

    override fun start() {
        if (isRunning) {
            logger.warn { "GRPC server is already running" }
            return
        }

        logger.info { "Starting GRPC server..." }
        try {
            tryToStart()
        } catch (e: Exception) {
            logger.error(e) { "Error while starting GRPC server" }
            runningState?.execController?.close()
            stop()
        }
    }

    private fun tryToStart() {
        val definitionBuild = buildUserDefinition()
        val config = definitionBuild.serverConfig
        definitionBuild.onError { handleDefinitionError(config, it) }

        val execController = buildExecController(config)
        val serverRegistry = buildServerRegistry(config, execController, definitionBuild)
        val servicesGraph = ServicesGraph(serverRegistry)


        ExecThreadBinding.bind(true, execController)
        servicesGraph.start(DefaultEvent(serverRegistry, reloading))

        val server = GrpcServer(
            execController = execController!!,
            services = serverRegistry.getAll(BindableService::class.java).toList(),
            configuration = GrpcConfiguration(config.port, true)
        ).also { it.start() }

        runningState = RunningState(
            server = server,
            execController = execController,
            useSsl = config.sslContext != null,
            applicationState = ApplicationState(
                definitionBuild = definitionBuild,
                serverRegistry = serverRegistry,
                servicesGraph = servicesGraph
            )
        )

        logger.info { "GRPC server started ${scheme}://${bindHost}:${bindPort}" }

        registerShutdownHook(config)
    }

    override fun stop() {
        if (!isRunning) {
            return
        }
        try {
            try {
                runningState?.applicationState?.servicesGraph?.stop(
                    DefaultEvent(
                        runningState?.applicationState?.serverRegistry,
                        reloading
                    )
                )
            } finally {
                runningState?.server?.stop()
                runningState?.execController?.close()
            }
        } finally {
            runningState?.stopLatch?.countDown()
            runningState = null
        }
        logger.info { "GRPC server stopped" }
    }

    override fun await(timeout: Duration): Boolean {
        return runningState?.stopLatch?.await(timeout.toMillis(), MILLISECONDS) ?: true
    }

    override fun reload(): RatpackServer {
        reloading = true
        if(isRunning) {
            stop()
        }
        start()
        reloading = false
        return this
    }

    override fun getRegistry(): Optional<Registry> = Optional.ofNullable(runningState?.applicationState?.serverRegistry)

    private fun buildServerRegistry(
        config: ServerConfig,
        execController: ExecControllerInternal,
        definitionBuild: DefinitionBuild
    ): Registry = ServerRegistry.serverRegistry(
        this, impositions, execController, config, definitionBuild.userRegistryFactory
    )

    private fun buildExecController(config: ServerConfig) =
        DefaultExecController.of { definition ->
            definition.compute { it.threads(config.threads) }
        } as ExecControllerInternal


    private fun registerShutdownHook(config: ServerConfig) {
        if (config.isRegisterShutdownHook) {
            shutdownHookThread = Thread({ stop() }, "grpc-shutdown-thread")
            Runtime.getRuntime().addShutdownHook(shutdownHookThread)
        }
    }

    private fun handleDefinitionError(serverConfig: ServerConfig, error: Throwable) {
        if (serverConfig.isDevelopment) {
            logger.error(error) { "Exception raised getting server config (will use default config until reload)" }
            needsReload.set(true)
        } else {
            throw Exceptions.toException(error)
        }
    }

    private fun buildUserDefinition(): DefinitionBuild =
        Impositions.impose(impositions) {
            try {
                DefinitionBuild(impositions, RatpackServerDefinition.build(definitionFactory))
            } catch (e: Exception) {
                DefinitionBuild(
                    impositions,
                    errorServer(e),
                    e
                )
            }
        }

    private fun errorServer(e: Exception): RatpackServerDefinition =
        RatpackServerDefinition.build { server -> server.handler { _ -> Handler { ctx -> ctx.error(e) } } }


    private class RunningState(
        val server: GrpcServer,
        val execController: ExecControllerInternal,
        val useSsl: Boolean,
        val applicationState: ApplicationState
    ) {
        val stopLatch = CountDownLatch(1)
    }


    private class ApplicationState(
        val definitionBuild: DefinitionBuild,
        val serverRegistry: Registry,
        val servicesGraph: ServicesGraph
    )


    private class DefinitionBuild(
        val impositions: Impositions,
        private val definition: RatpackServerDefinition,
        private val error: Throwable? = null
    ) {
        val serverConfig: ServerConfig
            get() = definition.serverConfig


        val userRegistryFactory: RatpackFunction<in Registry, out Registry> =
            RatpackFunction<Registry, Registry> { baseRegistry ->
                definition
                    .registry
                    .apply(baseRegistry)
                    .let { it.join(userRegistryImposition().build(it)) }
            }

        private fun userRegistryImposition(): UserRegistryImposition =
            impositions
                .getAll(UserRegistryImposition::class.java)
                .firstOrNull()
                ?: UserRegistryImposition.none()

        fun onError(handler: (Throwable) -> Unit) {
            error?.apply(handler)
        }
    }

    companion object: KLogging()

}