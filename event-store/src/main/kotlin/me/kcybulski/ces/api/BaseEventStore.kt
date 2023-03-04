package me.kcybulski.ces.api

import me.kcybulski.ces.api.PublishingResult.Success
import mu.KotlinLogging.logger

class BaseEventStore(
    private val repository: EventsRepository
) : EventStore {

    private val logger = logger { }

    override suspend fun <A : Any> publish(
        event: Event<A>,
        stream: Stream,
        expectedSequenceNumber: ExpectedSequenceNumber
    ): PublishingResult {
        logger.info { "Publishing new event to ${stream.id}" }
        return Success(EventId("1001"))
    }

    override suspend fun read(readQuery: ReadQuery): EventStream =
        ListEventStream(emptyList())

    override suspend fun <T : Any> subscribe(type: String, handler: suspend (T) -> Unit) {
        logger.info { "Registering subscriber for $type" }
    }

}