package me.kcybulski.ces.eventstore.base

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventId
import me.kcybulski.ces.eventstore.EventSerializer
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.EventStream
import me.kcybulski.ces.eventstore.EventsRepository
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.AnySequenceNumber
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.SpecificSequenceNumber
import me.kcybulski.ces.eventstore.PublishingResult
import me.kcybulski.ces.eventstore.PublishingResult.Failure.InvalidExpectedSequenceNumber
import me.kcybulski.ces.eventstore.PublishingResult.Success
import me.kcybulski.ces.eventstore.ReadQuery
import me.kcybulski.ces.eventstore.ReadQuery.AllEvents
import me.kcybulski.ces.eventstore.ReadQuery.SpecificEvent
import me.kcybulski.ces.eventstore.ReadQuery.SpecificStream
import me.kcybulski.ces.eventstore.SaveEventResult.OptimisticLockingError
import me.kcybulski.ces.eventstore.SaveEventResult.Saved
import me.kcybulski.ces.eventstore.SerializedEvent
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.StreamedEvent
import me.kcybulski.ces.eventstore.tasks.SubscriptionsRegistry
import mu.KotlinLogging.logger
import java.time.Clock
import java.util.UUID.randomUUID

internal class BaseEventStore(
    private val repository: EventsRepository,
    private val serializer: EventSerializer,
    private val subscriptionsRegistry: SubscriptionsRegistry,
    private val clock: Clock
) : EventStore {

    private val logger = logger { }

    override suspend fun <A : Any> publish(
        event: Event<A>,
        stream: Stream,
        expectedSequenceNumber: ExpectedSequenceNumber
    ): PublishingResult {
        logger.info { "Publishing new event to ${stream.id}" }
        val eventId = EventId(randomUUID().toString())
        val serializedEvent = SerializedEvent(
            id = eventId,
            stream = stream,
            timestamp = clock.instant(),
            type = event.type,
            subscribers = subscriptionsRegistry.subscribersNamesForType(event.type).toMutableList(),
            sequenceNumber = when (expectedSequenceNumber) {
                AnySequenceNumber -> null
                is SpecificSequenceNumber -> expectedSequenceNumber.number
            },
            payload = serializer.serialize(event.payload),
            _class = event.className
        )
        return when (repository.save(serializedEvent)) {
            Saved -> Success(eventId)
            OptimisticLockingError -> InvalidExpectedSequenceNumber(expectedSequenceNumber as SpecificSequenceNumber)
        }
    }

    override suspend fun read(readQuery: ReadQuery): EventStream =
        when (readQuery) {
            AllEvents -> repository.loadAll().map { streamSerializedEvent(it) }
            is SpecificEvent -> repository
                .findEvent(readQuery.eventId)
                ?.let { listOf(streamSerializedEvent(it)) }
                ?: emptyList()

            is SpecificStream -> repository.loadStream(readQuery.stream).map { streamSerializedEvent(it) }
        }
            .let { ListEventStream(it) }

    private suspend fun streamSerializedEvent(serializedEvent: SerializedEvent) =
        StreamedEvent(
            id = serializedEvent.id,
            timestamp = serializedEvent.timestamp,
            type = serializedEvent.type,
            payload = serializer.deserialize(serializedEvent.payload, serializedEvent._class)
        )

    override suspend fun <T> subscribe(name: String, type: String, handler: suspend (T) -> Unit) {
        subscriptionsRegistry.subscribe(name, type, handler)
    }
}