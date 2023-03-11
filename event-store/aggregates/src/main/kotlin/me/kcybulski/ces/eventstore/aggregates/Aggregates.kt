package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.ReadQuery.SpecificStream
import me.kcybulski.ces.eventstore.Stream
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.primaryConstructor

class Aggregates internal constructor(
    val eventStore: EventStore
) {

    suspend inline fun <reified T : Aggregate<T>> load(stream: Stream): T? {
        val events = eventStore
            .read(SpecificStream(stream))
            .collectList()
        if(events.isEmpty()) return null
        val creator = T::class.companionObjectInstance as? AggregateCreator<T, Event<*>>
        val firstEvent: Event<*> = events.first().payload as Event<*>
        val aggregate = creator?.from(firstEvent)
            ?: T::class.primaryConstructor?.call()
            ?: return null
        return events.drop(1).fold(aggregate) { agg, event -> agg.apply(event.payload as Event<*>) }
    }

    suspend fun <T : Aggregate<T>> save(aggregate: T): SaveAggregateResult {
        aggregate
            .unpublishedEvents
            .forEach { eventStore.publish(it as Event<Any>, aggregate.stream) }
        return SaveAggregateResult.AggregateSaved(aggregate)
    }

    companion object {

        fun onEventStore(eventStore: EventStore) = Aggregates(eventStore)
    }
}

sealed interface SaveAggregateResult {

    data class AggregateSaved<T : Aggregate<T>>(val aggregate: T): SaveAggregateResult

}