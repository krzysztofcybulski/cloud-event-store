package me.kcybulski.ces.service.grpc

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.netty.buffer.ByteBufAllocator.DEFAULT
import io.netty.channel.ChannelOption.ALLOCATOR
import ratpack.core.service.Service
import ratpack.exec.ExecController
import ratpack.exec.util.internal.TransportDetector.getServerSocketChannelImpl
import java.net.InetSocketAddress

class GrpcServer(
    private val execController: ExecController,
    private val services: List<BindableService>,
    private val configuration: GrpcConfiguration
) : Service {

    private lateinit var shutdownHook: Thread
    private lateinit var nettyServer: Server

    val port: Int
        get() = nettyServer.port

    val address: InetSocketAddress by lazy { configuration.inetAddress() }

    val isRunning: Boolean
        get() = !nettyServer.isShutdown

    fun start() {
        nettyServer = NettyServerBuilder
            .forAddress(configuration.inetAddress())
            .channelType(getServerSocketChannelImpl())
            .withChildOption(ALLOCATOR, DEFAULT)
            .bossEventLoopGroup(execController.eventLoopGroup)
            .workerEventLoopGroup(execController.eventLoopGroup)
            .executor(execController.executor)
            .addAllServices()
            .addReflection()
            .build()
            .start()
            .also { registerShutdownHook(it) }
    }

    private fun registerShutdownHook(server: Server) {
        shutdownHook = Thread({
            server.shutdownNow()
        }, "grpc-shutdown-thread")
        Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    fun stop() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook)
        } catch (_: Exception) {
        } finally {
                nettyServer.shutdown()
        }
    }

    private fun NettyServerBuilder.addAllServices(): NettyServerBuilder {
        services.forEach(this::addService)
        return this
    }

    private fun NettyServerBuilder.addReflection(): NettyServerBuilder {
        if (configuration.reflection) {
            addService(ProtoReflectionService.newInstance())
        }
        return this
    }
}

