package me.kcybulski.ces.api

import me.kcybulski.ces.api.PublishingResult.Success
import me.kcybulski.ces.api.ReadQuery.AllEvents
import me.kcybulski.ces.api.ReadQuery.SpecificEvent
import me.kcybulski.ces.api.ReadQuery.SpecificStream
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
            payload = serializer.serialize(event.payload),
            _class = event.javaClass.name
        )
        repository.save(serializedEvent)
        return Success(eventId)
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
            payload = serializer.deserialize(serializedEvent.payload, Class.forName(serializedEvent._class))
        )

    override suspend fun <T> subscribe(name: String, type: String, handler: suspend (T) -> Unit) {
        subscriptionsRegistry.subscribe(name, type, handler)
    }
}