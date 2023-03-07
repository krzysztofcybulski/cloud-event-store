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
class QueriesFacade(
    private val eventStore: EventStore
) {

    private val logger = KotlinLogging.logger {}

    suspend fun query(query: Query): Flow<StreamedEvent<*>> {
        logger.info { "Processing query: $query" }
        return eventStore.read(ReadQuery.SpecificStream(query.streamId))
            .collectList()
            .asFlow()
    }

    data class Query(
        val streamId: Stream
    )
}