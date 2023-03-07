package me.kcybulski.ces.mongo

import me.kcybulski.ces.eventstore.EventStoreConfigurationBuilder
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun EventStoreConfigurationBuilder.mongo(configure: MongoConfiguration.() -> Unit) {
    val configuration = MongoConfiguration().apply(configure)
    val client = KMongo.createClient(configuration.mongoUrl).coroutine
    val database = client.getDatabase(configuration.database)
    val repository = MongoEventsRepository(database)
    eventsRepository = repository
    tasksRepository = repository
}

class MongoConfiguration {

    lateinit var mongoUrl: String
    lateinit var database: String

}