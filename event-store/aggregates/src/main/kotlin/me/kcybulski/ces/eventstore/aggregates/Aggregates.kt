package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.Stream

class Aggregates(
    private val eventStore: EventStore
) {

    suspend fun <T : Aggregate<T>> load(stream: Stream, initialize: (Event<*>) -> T): T? {
        return null
    }

    suspend fun <T : Aggregate<T>> save(stream: Stream, aggregate: T) {
    }

}