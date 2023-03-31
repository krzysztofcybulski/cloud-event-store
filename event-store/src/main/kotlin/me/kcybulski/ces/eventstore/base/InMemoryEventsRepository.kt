package me.kcybulski.ces.eventstore.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.emptyFlow
import me.kcybulski.ces.eventstore.EventId
import me.kcybulski.ces.eventstore.EventsRepository
import me.kcybulski.ces.eventstore.SaveEventResult
import me.kcybulski.ces.eventstore.SaveEventResult.OptimisticLockingError
import me.kcybulski.ces.eventstore.SaveEventResult.Saved
import me.kcybulski.ces.eventstore.SerializedEvent
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.tasks.TasksRepository
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryEventsRepository : EventsRepository, TasksRepository {

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

    override suspend fun loadAll(): Flow<SerializedEvent> {
        return memory.values.flatten().asFlow()
    }

    override suspend fun loadStreamFrom(stream: Stream, from: Long): Flow<SerializedEvent> {
        return memory[stream]?.asFlow()?.dropWhile { it.sequenceNumber!! < from } ?: emptyFlow()
    }

    override suspend fun findEvent(eventId: EventId): SerializedEvent? {
        return memory.values.flatten().find { it.id == eventId }
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