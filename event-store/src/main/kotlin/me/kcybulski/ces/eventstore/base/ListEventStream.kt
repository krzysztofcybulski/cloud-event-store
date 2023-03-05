package me.kcybulski.ces.eventstore.base

import me.kcybulski.ces.eventstore.EventId
import me.kcybulski.ces.eventstore.EventStream
import me.kcybulski.ces.eventstore.StreamedEvent

internal class ListEventStream(
    private val streamedEvents: List<StreamedEvent<*>>
) : EventStream {
    override suspend fun startingFrom(eventId: EventId): EventStream =
        ListEventStream(streamedEvents.dropWhile { it.id != eventId })

    override suspend fun backwards(): EventStream =
        ListEventStream(streamedEvents.reversed())

    override suspend fun collectList(): List<StreamedEvent<*>> =
        streamedEvents

    override suspend fun <T> project(initial: T, mapper: (T, Any) -> T): T =
        streamedEvents
            .map(StreamedEvent<*>::payload)
            .fold(initial, mapper)
}