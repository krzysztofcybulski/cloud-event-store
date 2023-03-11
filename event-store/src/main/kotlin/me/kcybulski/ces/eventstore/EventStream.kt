package me.kcybulski.ces.eventstore

import java.time.Instant

interface EventStream {

    suspend fun startingFrom(eventId: EventId): EventStream
    suspend fun backwards(): EventStream

    suspend fun collectList(): List<StreamedEvent<*>>

    suspend fun <T> project(initial: T, mapper: (T, Any) -> T): T

}

data class StreamedEvent<T : Any>(
    val id: EventId,
    val timestamp: Instant,
    override val type: String,
    override val className: String,
    override val payload: T
): Event<T>