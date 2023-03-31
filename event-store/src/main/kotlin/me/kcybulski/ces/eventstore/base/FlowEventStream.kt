package me.kcybulski.ces.eventstore.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import me.kcybulski.ces.eventstore.EventId
import me.kcybulski.ces.eventstore.EventStream
import me.kcybulski.ces.eventstore.StreamedEvent

internal class FlowEventStream(
    private val streamedEvents: Flow<StreamedEvent<*>>
) : EventStream {
    override suspend fun startingFrom(eventId: EventId): EventStream =
        FlowEventStream(streamedEvents.dropWhile { it.id != eventId })

    // TODO
    override suspend fun backwards(): EventStream =
        FlowEventStream(streamedEvents.toList().reversed().asFlow())

    override suspend fun collectList(): List<StreamedEvent<*>> =
        streamedEvents.toList()

    override suspend fun <T> project(initial: T, mapper: (T, Any) -> T): T =
        streamedEvents
            .map { it.payload }
            .fold(initial, mapper)

    override suspend fun flow(): Flow<StreamedEvent<*>> =
        streamedEvents
}