package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.ReadQuery.SpecificStream
import me.kcybulski.ces.eventstore.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObjectInstance

class Aggregates internal constructor(
    val eventStore: EventStore
) {

    suspend inline fun <reified T : Aggregate<T>> load(stream: Stream): T? {
        val (head, tail) = readEvents(stream) ?: return null
        return tail.fold(initialState(head)) { agg, event -> agg?.apply(event.payload as Event<*>) }
    }

    suspend fun <T : Aggregate<T>> save(aggregate: T): SaveAggregateResult {
        aggregate
            .unpublishedEvents
            .forEach { eventStore.publish(it as Event<Any>, aggregate.stream) }
        aggregate.unpublishedEvents.clear()
        return SaveAggregateResult.AggregateSaved(aggregate)
    }

    suspend fun readEvents(stream: Stream) = eventStore
        .read(SpecificStream(stream))
        .collectList()
        .map { it.payload as Event<*> }
        .takeIf { it.isNotEmpty() }
        ?.let { it.first() to it.drop(1) }
    
    inline fun <reified T : Aggregate<T>> initialState(initialEvent: Event<*>): T? =
        T::class.aggregateCreator
            ?.from(initialEvent)
            ?: T::class.callEmptyConstructor()?.apply(initialEvent)

    companion object {

        fun onEventStore(eventStore: EventStore) = Aggregates(eventStore)
    }
}

sealed interface SaveAggregateResult {

    data class AggregateSaved<T : Aggregate<T>>(val aggregate: T) : SaveAggregateResult

}

inline val <reified T : Aggregate<T>> KClass<T>.aggregateCreator
    get() = T::class.companionObjectInstance as? AggregateCreator<T, Event<*>>

inline fun <reified T : Aggregate<T>> KClass<T>.callEmptyConstructor() =
    constructors
        .find { it.parameters.all(KParameter::isOptional) }
        ?.callBy(emptyMap())