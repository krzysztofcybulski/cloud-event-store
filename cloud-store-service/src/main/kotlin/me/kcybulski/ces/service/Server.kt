package me.kcybulski.ces.service

import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.EventStoreConfiguration
import me.kcybulski.ces.eventstore.EventStoreConfiguration.eventStore
import me.kcybulski.ces.service.api.EventStoreResource
import me.kcybulski.ces.service.grpc.GrpcRatpackServer
import ratpack.core.server.RatpackServer
import ratpack.exec.registry.Registry

class Server(
    private val eventStore: EventStore,
    private val configuration: ServerConfiguration
) {

    private val grpcServer = GrpcRatpackServer.of { server ->
        server
            .serverConfig { config ->
                config
                    .port(configuration.grpcPort)
                    .threads(configuration.grpcThreads)
            }
            .registry { registry ->
                registry.join(Registry.of {
                    it.add(EventStoreResource(eventStore))
                })
            }
    }

    private val httpServer = RatpackServer.of { server ->
        server
            .serverConfig { config ->
                config
                    .port(configuration.httpPort)
                    .threads(configuration.httpThreads)
            }
            .handlers { chain ->
                chain.get { ctx -> ctx.render("Hello!") }
            }
    }

    fun start() {
        if (configuration.httpEnabled) {
            httpServer.start()
        }
        if (configuration.grpcEnabled) {
            grpcServer.start()
        }
    }

    fun stop() {
        httpServer.stop()
        grpcServer.stop()
    }
}

data class ServerConfiguration(
    val httpEnabled: Boolean,
    val httpPort: Int,
    val httpThreads: Int,
    val grpcEnabled: Boolean,
    val grpcPort: Int,
    val grpcThreads: Int
)