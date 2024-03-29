package me.kcybulski.ces.service

import io.micrometer.prometheus.PrometheusMeterRegistry
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.service.api.EventStoreResource
import me.kcybulski.ces.service.grpc.GrpcRatpackServer
import ratpack.core.server.RatpackServer

class Server(
    private val eventStore: EventStore,
    private val prometheusMeterRegistry: PrometheusMeterRegistry,
    private val configuration: ServerConfiguration
) {


    private val httpServer = RatpackServer.of { server ->
        server
            .serverConfig { config ->
                config
                    .port(configuration.httpPort)
                    .threads(configuration.httpThreads)
            }
            .handlers { chain ->
                chain
                    .get("metrics") {
                        it.render(prometheusMeterRegistry.scrape())
                    }
            }
    }

    private val grpcServer = GrpcRatpackServer.of { server ->
        server
            .serverConfig { config ->
                config
                    .port(configuration.grpcPort)
                    .threads(configuration.grpcThreads)
            }
            .registryOf { registry ->
                registry.add(EventStoreResource(eventStore))
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