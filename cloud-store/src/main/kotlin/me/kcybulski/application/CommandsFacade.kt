package me.kcybulski.application

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber
import me.kcybulski.ces.eventstore.Stream
import mu.KotlinLogging
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class CommandsFacade(
    private val eventStore: EventStore
) {

    private val logger = KotlinLogging.logger {}

    suspend fun publishEvents(command: PublishEventCommand) {
        logger.info { "Processing publish event command: $command" }
        eventStore.publish(
            event = CloudEvent(command.type, command.payload),
            stream = command.stream,
            expectedSequenceNumber = command.expectedSequenceNumber
        )
    }

    data class PublishEventCommand(
        val stream: Stream,
        val type: String,
        val expectedSequenceNumber: ExpectedSequenceNumber,
        val payload: Any
    )

}

internal class CloudEvent(
    override val type: String,
    override val payload: Any
) : Event<Any> {

    override val className = "me.kcybulski.ces.CloudEvent"

}