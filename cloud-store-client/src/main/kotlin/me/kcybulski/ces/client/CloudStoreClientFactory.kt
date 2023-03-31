package me.kcybulski.ces.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.grpc.ManagedChannelBuilder
import me.kcybulski.ces.eventstore.EventStore

object CloudStoreClientFactory {

    fun cloudStoreClient(configure: CloudStoreClientConfiguration.() -> Unit): EventStore {
        val configuration = CloudStoreClientConfiguration().apply(configure)
        return CloudStoreClient(
            managedChannel = managedChannel(configuration.address),
            mapper = GrpcMappers(configuration.objectMapper)
        )
    }

    private fun managedChannel(address: String) =
        ManagedChannelBuilder
            .forTarget(address)
            .usePlaintext()
            .build()

}

class CloudStoreClientConfiguration {

    lateinit var address: String
    var objectMapper: ObjectMapper = jacksonObjectMapper()

}