package me.kcybulski.ces.api

import me.kcybulski.ces.api.SaveEventResult.OptimisticLockingError
import me.kcybulski.ces.api.SaveEventResult.Saved
import java.util.concurrent.ConcurrentHashMap

class InMemoryEventsRepository : EventsRepository, TasksRepository {

    private val memory: ConcurrentHashMap<Stream, List<SerializedEvent>> = ConcurrentHashMap()

    // TODO Thread safe implementation
    override suspend fun save(event: SerializedEvent): SaveEventResult {
        val streamLength: Long = memory[event.stream]?.size?.toLong() ?: 0L
        if(event.sequenceNumber != null && event.sequenceNumber != streamLength) {
            return OptimisticLockingError
        }
        val eventToSave = event.copy(
            sequenceNumber = event.sequenceNumber ?: streamLength
        )
        memory.merge(event.stream, listOf(eventToSave)) { a, b -> a + b }
        return Saved
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