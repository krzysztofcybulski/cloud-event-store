package me.kcybulski.application

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.ReadQuery
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.StreamedEvent
import mu.KotlinLogging
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class StreamEvents(
    private val eventStore: EventStore
) {

    private val logger = KotlinLogging.logger {}

    suspend fun streamEvents(streamId: Stream): Flow<StreamedEvent<*>> {
        logger.debug { "Streaming events from $streamId" }
        return eventStore.read(ReadQuery.SpecificStream(streamId))
            .collectList()
            .asFlow()
    }
}