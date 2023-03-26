package me.kcybulski.ces.eventstore.tasks

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.StreamedEvent
import me.kcybulski.ces.eventstore.tasks.RunHandlerResult.ErrorWhileHandling
import me.kcybulski.ces.eventstore.tasks.RunHandlerResult.HandledSuccessfully
import me.kcybulski.ces.eventstore.tasks.RunHandlerResult.NoHandlerFound
import mu.KotlinLogging

internal class SubscriptionsRegistry {
    private val logger = KotlinLogging.logger { }

    private val subscribers: MutableList<Subscriber> = mutableListOf()

    suspend fun <T: Any> subscribe(name: String, type: String, handler: suspend (StreamedEvent<T>) -> Unit) {
        logger.info { "Registering subscriber for $type" }
        subscribers += Subscriber(type, name, handler as suspend (StreamedEvent<Any>) -> Unit)
    }

    suspend fun subscribersNamesForType(type: String): List<String> {
        return subscribers.filter { it.eventType == type || it.eventType == "*" }.map { it.name }
    }

    suspend fun runSubscriptionFor(name: String, event: StreamedEvent<Any>): RunHandlerResult {
        logger.info { "Name: $name, subscribers: $subscribers" }
        val subscriber = subscribers.find { it.name == name } ?: return NoHandlerFound
        return try {
            subscriber.handler(event)
            HandledSuccessfully(subscriber.name)
        } catch (cause: Throwable) {
            ErrorWhileHandling(cause)
        }
    }

}

sealed interface RunHandlerResult {

    data class HandledSuccessfully(val subscriberName: String) : RunHandlerResult
    object NoHandlerFound : RunHandlerResult
    class ErrorWhileHandling(val cause: Throwable) : RunHandlerResult

}

internal data class Subscriber(
    val eventType: String,
    val name: String,
    val handler: suspend (StreamedEvent<Any>) -> Unit
)