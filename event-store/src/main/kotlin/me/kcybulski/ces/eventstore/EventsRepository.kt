package me.kcybulski.ces.eventstore

import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface EventsRepository {

    suspend fun save(event: SerializedEvent): SaveEventResult

    suspend fun loadAll(): Flow<SerializedEvent>
    suspend fun loadStreamFrom(stream: Stream, from: Long = 0): Flow<SerializedEvent>
    suspend fun findEvent(eventId: EventId): SerializedEvent?

}

sealed interface SaveEventResult {

    object Saved : SaveEventResult
    object OptimisticLockingError : SaveEventResult

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
) {

    suspend fun asStreamedEvent(serializer: EventSerializer) =
        StreamedEvent(
            id = id,
            timestamp = timestamp,
            stream = stream,
            type = type,
            className = _class,
            payload = serializer.deserialize(payload, _class)
        )
}

@JvmInline
value class EventId(val raw: String)