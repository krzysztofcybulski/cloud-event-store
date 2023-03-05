package me.kcybulski.ces.api

import java.time.Instant

interface EventsRepository {

    suspend fun save(event: SerializedEvent): SaveEventResult

    suspend fun loadAll(): List<SerializedEvent>
    suspend fun loadStream(stream: Stream): List<SerializedEvent>
    suspend fun findEvent(eventId: EventId): SerializedEvent?

}

sealed interface SaveEventResult {

    object Saved: SaveEventResult
    object OptimisticLockingError: SaveEventResult

}

data class SerializedEvent(
    val id: EventId,
    val stream: Stream,
    val type: String,
    val timestamp: Instant,
    val subscribers: MutableList<String>,
    val sequenceNumber: Long?,
    val payload: String,
    val _class: String
)

@JvmInline
value class EventId(val raw: String)