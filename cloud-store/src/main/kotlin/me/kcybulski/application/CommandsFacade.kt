package me.kcybulski.application

import mu.KotlinLogging
import java.time.Instant

class CommandsFacade {

    private val logger = KotlinLogging.logger {}

    suspend fun publishEvents(command: PublishEventsCommand) {
        logger.info { "Publish events $command" }
    }

    data class PublishEventsCommand(
        val events: List<Event>
    ) {

        data class Event(
            val stream: String,
            val type: String,
            val timestamp: Instant,
            val payload: Any
        )

    }

}