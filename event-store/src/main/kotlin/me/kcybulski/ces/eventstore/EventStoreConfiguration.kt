package me.kcybulski.ces.eventstore

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import me.kcybulski.ces.eventstore.base.BaseEventStore
import me.kcybulski.ces.eventstore.base.InMemoryEventsRepository
import me.kcybulski.ces.eventstore.tasks.CoroutinesTaskProcessingScheduler
import me.kcybulski.ces.eventstore.tasks.SubscriptionsRegistry
import me.kcybulski.ces.eventstore.tasks.TasksProcessor
import java.time.Clock

object EventStoreConfiguration {

    fun inMemoryEventStoreWithCoroutineTasksProcessing(): EventStore {
        val subscriptionsRegistry = SubscriptionsRegistry()
        val repository = InMemoryEventsRepository()
        val serializer = JsonEventSerializer(jacksonObjectMapper())
        val tasksProcessor = TasksProcessor(
            repository = repository,
            serializer = serializer,
            subscriptionsRegistry = subscriptionsRegistry
        )
        runBlocking {
            CoroutinesTaskProcessingScheduler(tasksProcessor).start()
        }
        return BaseEventStore(
            repository = repository,
            serializer = serializer,
            clock = Clock.systemUTC(),
            subscriptionsRegistry = subscriptionsRegistry
        )
    }

}