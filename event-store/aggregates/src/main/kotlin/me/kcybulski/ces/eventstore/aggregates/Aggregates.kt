package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.Stream

class Aggregates internal constructor(
    private val eventStore: EventStore
) {

    suspend fun <T : Aggregate<T>> load(stream: Stream): T? {
        return null
    }

    suspend fun <T : Aggregate<T>> save(aggregate: T) {
    }

    companion object {

        fun onEventStore(eventStore: EventStore) = Aggregates(eventStore)
    }
}

sealed interface SaveAggregateResult {

    data class AggregateSaved<T: Aggregate<T>>(val aggregate: T)

}