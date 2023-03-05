package me.kcybulski.ces.api

import me.kcybulski.ces.api.RunHandlerResult.ErrorWhileHandling
import me.kcybulski.ces.api.RunHandlerResult.HandledSuccessfully
import mu.KotlinLogging.logger

internal class TasksProcessor(
    private val repository: TasksRepository,
    private val serializer: EventSerializer,
    private val subscriptionsRegistry: SubscriptionsRegistry
) {

    private val logger = logger { }

    suspend fun processNext() {
        val taskToProcess = repository.findUnprocessedTask()
        if (taskToProcess != null) {
            logger.info { "Processing ${taskToProcess.id}, type ${taskToProcess.type}" }
            val subscriberName = taskToProcess.subscribers.first()
            val payload = serializer.deserialize(taskToProcess.payload, Class.forName(taskToProcess._class))
            val handlerResult = subscriptionsRegistry.runSubscriptionFor(subscriberName, taskToProcess.type, payload)
            when (handlerResult) {
                is ErrorWhileHandling -> {
                    logger.error { "Error while handling $subscriberName for ${taskToProcess.id}" }
                }

                is HandledSuccessfully -> {
                    logger.info { "Handled successfully $subscriberName in ${taskToProcess.id}" }
                    repository.processed(taskToProcess.id, subscriberName)
                }

                RunHandlerResult.NoHandlerFound -> {
                    logger.warn { "No subscriber $subscriberName found for ${taskToProcess.id}" }
                }
            }
        }
    }
}
