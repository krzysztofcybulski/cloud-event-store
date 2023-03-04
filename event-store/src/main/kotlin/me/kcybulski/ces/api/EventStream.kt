package me.kcybulski.ces.api

import java.time.Instant

interface EventStream {

    suspend fun startingFrom(eventId: EventId): EventStream
    suspend fun backwards(): EventStream

    suspend fun collectList(): List<StreamedEvent<*>>

    suspend fun <T> project(initial: T, mapper: (T, Any) -> T): T

}

data class StreamedEvent<T: Any>(
    val id: EventId,
    val timestamp: Instant,
    val type: String,
    val payload: T
)