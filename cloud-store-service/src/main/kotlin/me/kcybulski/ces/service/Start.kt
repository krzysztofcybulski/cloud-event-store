package me.kcybulski.ces.service

import me.kcybulski.ces.eventstore.EventStoreConfiguration.eventStore

fun main() {
    val eventStore = eventStore {
        inMemory()
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