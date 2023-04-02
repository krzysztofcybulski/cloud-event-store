package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.SpecificSequenceNumber
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
        val aggregate: T? = tail.fold(initialState(head)) { agg, event -> agg?.apply(event.payload as Event<*>) }
        aggregate?.version = tail.size.toLong()
        return aggregate
    }

    suspend fun <T : Aggregate<T>> save(aggregate: T): SaveAggregateResult {
        aggregate
            .unpublishedEvents
            .forEachIndexed { index, event ->
                eventStore.publish(
                    event = event as Event<Any>,
                    stream = aggregate.stream,
                    expectedSequenceNumber = SpecificSequenceNumber(aggregate.version + index + 1)
                )
            }
        aggregate.version += aggregate.unpublishedEvents.size
        aggregate.unpublishedEvents = listOf()
        return AggregateSaved(aggregate)
    }

    suspend inline fun <reified T : Aggregate<T>> update(stream: Stream, update: (T) -> T): UpdateAggregateResult =
        load<T>(stream)
            ?.let(update)
            ?.let { save(it) as UpdateAggregateResult }
            ?: NoAggregateFound(stream)

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

sealed interface SaveAggregateResult
sealed interface UpdateAggregateResult

data class AggregateSaved<T : Aggregate<T>>(val aggregate: T) : SaveAggregateResult, UpdateAggregateResult
data class NoAggregateFound(val stream: Stream) : UpdateAggregateResult


inline val <reified T : Aggregate<T>> KClass<T>.aggregateCreator
    get() = T::class.companionObjectInstance as? AggregateCreator<T, Event<*>>

inline fun <reified T : Aggregate<T>> KClass<T>.callEmptyConstructor() =
    constructors
        .find { it.parameters.all(KParameter::isOptional) }
        ?.callBy(emptyMap())