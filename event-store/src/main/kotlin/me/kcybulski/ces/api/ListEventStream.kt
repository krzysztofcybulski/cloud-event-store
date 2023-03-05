package me.kcybulski.ces.api

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