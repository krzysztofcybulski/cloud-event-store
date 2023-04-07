package me.kcybulski.ces.eventstore

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import me.kcybulski.ces.eventstore.base.BaseEventStore
import me.kcybulski.ces.eventstore.base.InMemoryEventsRepository
import me.kcybulski.ces.eventstore.cache.InMemoryCacheEventsRepository
import me.kcybulski.ces.eventstore.tasks.CoroutinesTaskProcessingScheduler
import me.kcybulski.ces.eventstore.tasks.SubscriptionsRegistry
import me.kcybulski.ces.eventstore.tasks.TasksProcessor
import me.kcybulski.ces.eventstore.tasks.TasksRepository
import java.time.Clock

object EventStoreConfiguration {

    fun eventStore(configuration: EventStoreConfigurationBuilder.() -> Unit): EventStore =
        EventStoreConfigurationBuilder()
            .apply(configuration)
            .eventStore()

}

class EventStoreConfigurationBuilder {


    lateinit var tasksRepository: TasksRepository
    lateinit var eventsRepository: EventsRepository
    lateinit var serializer: EventSerializer
    private var eventsCache: Boolean = false
    private var metrics: MeterRegistry? = null

    init {
        inMemory()
        jacksonSerialization()
    }

    fun inMemory() {
        val repository = InMemoryEventsRepository()
        tasksRepository = repository
        eventsRepository = repository
    }

    fun jacksonSerialization() {
        serializer = JsonEventSerializer(jacksonObjectMapper())
    }

    fun noSerialization() {
        serializer = NoopSerializer()
    }

    fun withEventsCache() {
        eventsCache = true
    }

    fun noCache() {
        eventsCache = false
    }

    fun withMetrics(meterRegistry: MeterRegistry) {
        metrics = meterRegistry
    }

    fun noMetrics() {
        metrics = null
    }

    internal fun eventStore(): EventStore {
        val subscriptionsRegistry = SubscriptionsRegistry()
        var repository = eventsRepository
        if (eventsCache) {
            repository = InMemoryCacheEventsRepository(repository)
        }
        val tasksProcessor = TasksProcessor(
            repository = tasksRepository,
            serializer = serializer,
            subscriptionsRegistry = subscriptionsRegistry
        )
        runBlocking {
            CoroutinesTaskProcessingScheduler(tasksProcessor).start()
        }
        var eventStore: EventStore = BaseEventStore(
            repository = repository,
            serializer = serializer,
            clock = Clock.systemUTC(),
            subscriptionsRegistry = subscriptionsRegistry
        )
        if (metrics != null) {
            eventStore = MeteredEventStore(eventStore, metrics!!)
        }
        return eventStore
    }
}