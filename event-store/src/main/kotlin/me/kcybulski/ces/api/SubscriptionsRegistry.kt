package me.kcybulski.ces.api

import me.kcybulski.ces.api.RunHandlerResult.ErrorWhileHandling
import me.kcybulski.ces.api.RunHandlerResult.HandledSuccessfully
import me.kcybulski.ces.api.RunHandlerResult.NoHandlerFound
import mu.KotlinLogging

internal class SubscriptionsRegistry {
    private val logger = KotlinLogging.logger { }

    private val subscribers: MutableList<Subscriber> = mutableListOf()

    suspend fun <T> subscribe(name: String, type: String, handler: suspend (T) -> Unit) {
        logger.info { "Registering subscriber for $type" }
        subscribers += Subscriber(type, name, handler as suspend (Any) -> Unit)
    }

    suspend fun subscribersNamesForType(type: String): List<String> {
        return subscribers.filter { it.eventType == type }.map { it.name }
    }

    suspend fun runSubscriptionFor(name: String, type: String, payload: Any): RunHandlerResult {
        val subscriber = subscribers.find { it.name == name && it.eventType == type } ?: return NoHandlerFound
        return try {
            subscriber.handler(payload)
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

internal class Subscriber(
    val eventType: String,
    val name: String,
    val handler: suspend (Any) -> Unit
)