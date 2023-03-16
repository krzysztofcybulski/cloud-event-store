package me.kcybulski.ces.eventstore.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.toList
import me.kcybulski.ces.eventstore.EventId
import me.kcybulski.ces.eventstore.EventsRepository
import me.kcybulski.ces.eventstore.SaveEventResult
import me.kcybulski.ces.eventstore.SerializedEvent
import me.kcybulski.ces.eventstore.Stream

class InMemoryCacheEventsRepository(
    private val eventsRepository: EventsRepository
) : EventsRepository by eventsRepository {

    private val streams: MutableMap<Stream, List<SerializedEvent>> = mutableMapOf()
    private val events: MutableMap<EventId, SerializedEvent> = mutableMapOf()

    override suspend fun save(event: SerializedEvent): SaveEventResult {
        val result = eventsRepository.save(event)
        if (result is SaveEventResult.Saved) {
            streams.merge(event.stream, listOf(event), List<SerializedEvent>::plus)
            events[event.id] = event
        }
        return result
    }

    override suspend fun loadStreamFrom(stream: Stream, from: Long): Flow<SerializedEvent> {
        val cachedStream = streams[stream]?.drop(from.toInt()) ?: emptyList()
        val loadedStream =
            eventsRepository.loadStreamFrom(stream, cachedStream.size + from)
        val finalStream = merge(cachedStream.asFlow(), loadedStream)
        streams[stream] = finalStream.toList()
        return finalStream
    }

    override suspend fun findEvent(eventId: EventId): SerializedEvent? =
        events[eventId] ?: eventsRepository.findEvent(eventId)
}