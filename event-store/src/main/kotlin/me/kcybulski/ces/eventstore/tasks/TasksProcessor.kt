package me.kcybulski.ces.eventstore.tasks

import me.kcybulski.ces.eventstore.EventSerializer
import me.kcybulski.ces.eventstore.SerializedEvent
import me.kcybulski.ces.eventstore.tasks.RunHandlerResult.ErrorWhileHandling
import me.kcybulski.ces.eventstore.tasks.RunHandlerResult.HandledSuccessfully
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
            processTask(taskToProcess)
        }
    }

    private suspend fun processTask(taskToProcess: SerializedEvent) {
        val subscriberName = taskToProcess.subscribers.first()
        val handlerResult = subscriptionsRegistry.runSubscriptionFor(
            name = subscriberName,
            type = taskToProcess.type,
            payload = deserialize(taskToProcess)
        )
        when (handlerResult) {
            is ErrorWhileHandling -> {
                logger.error(handlerResult.cause) { "Error while handling $subscriberName for ${taskToProcess.id}" }
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

    private suspend fun deserialize(taskToProcess: SerializedEvent): Any =
        serializer.deserialize(taskToProcess.payload, Class.forName(taskToProcess._class))
}
