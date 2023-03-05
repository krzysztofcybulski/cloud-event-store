package me.kcybulski.ces.api

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

interface EventsRepository {

    suspend fun save(event: SerializedEvent)

    suspend fun loadAll(): List<SerializedEvent>
    suspend fun loadStream(stream: Stream): List<SerializedEvent>
    suspend fun findEvent(eventId: EventId): SerializedEvent?

}

class InMemoryEventsRepository : EventsRepository, TasksRepository {

    private val memory: MutableMap<Stream, List<SerializedEvent>> = mutableMapOf()

    override suspend fun save(event: SerializedEvent) {
        memory.merge(event.stream, listOf(event)) { a, b -> a + b }
    }

    override suspend fun loadAll(): List<SerializedEvent> {
        return memory.values.flatten()
    }

    override suspend fun loadStream(stream: Stream): List<SerializedEvent> {
        return memory[stream] ?: emptyList()
    }

    override suspend fun findEvent(eventId: EventId): SerializedEvent? {
        return loadAll().find { it.id == eventId }
    }

    override suspend fun processed(id: EventId, subscriberName: String) {
        memory.values.flatten()
            .find { it.id == id }
            ?.let {
                it.subscribers -= subscriberName
            }
    }

    override suspend fun findUnprocessedTask(): SerializedEvent? {
        return memory.values.flatten().find { it.subscribers.isNotEmpty() }
    }

}


data class SerializedEvent(
    val id: EventId,
    val stream: Stream,
    val type: String,
    val timestamp: Instant,
    val subscribers: MutableList<String>,
    val payload: String,
    val _class: String
)

@JvmInline
value class EventId(val raw: String)