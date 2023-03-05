package me.kcybulski.ces.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import java.time.Clock

object EventStoreConfiguration {

    fun inMemoryEventStore(): EventStore {
        val subscriptionsRegistry = SubscriptionsRegistry()
        val repository = InMemoryEventsRepository()
        val serializer = JsonEventSerializer(
            jacksonObjectMapper().addMixIn(
                SimpleEvent::class.java,
                IgnoreSimpleEventPayload::class.java
            )
        )
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