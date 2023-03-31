package me.kcybulski.ces.eventstore

import kotlinx.coroutines.flow.Flow
import me.kcybulski.ces.eventstore.base.FlowEventStream
import java.time.Instant

interface EventStream {

    suspend fun startingFrom(eventId: EventId): EventStream
    suspend fun backwards(): EventStream

    suspend fun collectList(): List<StreamedEvent<*>>

    suspend fun <T> project(initial: T, mapper: (T, Any) -> T): T

    suspend fun flow(): Flow<StreamedEvent<*>>

    companion object {

        fun from(events: Flow<StreamedEvent<*>>): EventStream = FlowEventStream(events)
    }
}

data class StreamedEvent<T : Any>(
    val id: EventId,
    val timestamp: Instant,
    val stream: Stream,
    override val type: String,
    override val className: String,
    override val payload: T
) : Event<T>