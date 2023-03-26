package me.kcybulski.ces.mongo

import com.fasterxml.jackson.databind.ObjectMapper
import com.quikommerce.ces.eventstore.management.ManagementFacade
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.EventStoreConfigurationBuilder
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.id.jackson.IdJacksonModule
import org.litote.kmongo.reactivestreams.KMongo

fun EventStoreConfigurationBuilder.mongo(configure: MongoConfiguration.() -> Unit) {
    val database = coroutineDatabase(configure)
    val repository = MongoEventsRepository(database)
    eventsRepository = repository
    tasksRepository = repository
}

fun mongoManagementFacade(eventStore: EventStore, objectMapper: ObjectMapper, configure: MongoConfiguration.() -> Unit): ManagementFacade {
    objectMapper.registerModule(IdJacksonModule())
    val database = coroutineDatabase(configure)
    return ManagementFacade(MongoStreamsMetadataRepository(database), eventStore)
}

private fun coroutineDatabase(configure: MongoConfiguration.() -> Unit): CoroutineDatabase {
    val configuration = MongoConfiguration().apply(configure)
    val client = KMongo.createClient(configuration.mongoUrl).coroutine
    val database = client.getDatabase(configuration.database)
    return database
}

class MongoConfiguration {

    lateinit var mongoUrl: String
    lateinit var database: String

}