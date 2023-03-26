package com.quikommerce.ces.eventstore.management

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.Stream
import mu.KotlinLogging.logger
import kotlin.math.log

class ManagementFacade(
    private val repository: StreamsMetadataRepository,
    eventStore: EventStore
) {

    private val logger = logger { }

    init {
        runBlocking {
            logger.info { "Registering" }
            eventStore.subscribe<Any>("metadata", "*") {
                logger.info { "Updating stream ${it.stream.id} metadata" }
                repository.updateStream(it.stream)
            }
        }
    }

    suspend fun findStream(id: Stream): StreamMetadata? =
        repository.findStream(id)

    suspend fun findStreams(type: String?): Flow<StreamMetadata> =
        repository.findStreams(type)
}