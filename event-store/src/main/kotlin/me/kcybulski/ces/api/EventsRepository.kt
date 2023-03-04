package me.kcybulski.ces.api

import java.time.Instant

interface EventsRepository {

    suspend fun save(events: List<SerializedEvent<*>>)

}

class InMemoryEventsRepository: EventsRepository {
    override suspend fun save(events: List<SerializedEvent<*>>) {
        TODO("Not yet implemented")
    }

}


data class SerializedEvent<T>(
    val id: EventId,
    val stream: Stream,
    val type: String,
    val timestamp: Instant,
    val payload: T
)

@JvmInline
value class EventId(val raw: String)