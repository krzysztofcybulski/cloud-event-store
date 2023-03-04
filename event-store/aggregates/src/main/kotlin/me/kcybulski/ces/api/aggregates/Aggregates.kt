package me.kcybulski.ces.api.aggregates

import me.kcybulski.ces.api.Event
import me.kcybulski.ces.api.EventStore
import me.kcybulski.ces.api.Stream

class Aggregates(
    private val eventStore: EventStore
) {

    suspend fun <T : Aggregate<T>> load(stream: Stream, initialize: (Event<*>) -> T): T? {
        return null
    }

    suspend fun <T : Aggregate<T>> save(stream: Stream, aggregate: T) {
    }

}