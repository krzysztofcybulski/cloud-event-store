package me.kcybulski.ces.service

import me.kcybulski.ces.eventstore.EventStoreConfiguration.eventStore
import me.kcybulski.ces.mongo.mongo

fun main() {
    val eventStore = eventStore {
        mongo {
            mongoUrl = "mongodb://localhost:27017"
            database = "event-store"
        }
        noSerialization()
    }

    val server = Server(
        eventStore,
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