package me.kcybulski.ces.service

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import me.kcybulski.ces.eventstore.EventStoreConfiguration.eventStore
import me.kcybulski.ces.mongo.mongo

fun main() {

    val metrics = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val eventStore = eventStore {
        mongo {
            mongoUrl = "mongodb://localhost:27017"
            database = "event-store"
        }
        withEventsCache()
        noSerialization()
        withMetrics(metrics)
    }

    val server = Server(
        eventStore,
        metrics,
        ServerConfiguration(
            httpEnabled = true,
            httpPort = 8080,
            httpThreads = 1,
            grpcEnabled = true,
            grpcPort = 5000,
            grpcThreads = 1
        )
    )

    server.start()
    Runtime.getRuntime().addShutdownHook(Thread { server.stop() })
}