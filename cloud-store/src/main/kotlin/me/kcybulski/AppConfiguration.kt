package me.kcybulski

import com.fasterxml.jackson.databind.ObjectMapper
import com.quikommerce.ces.eventstore.management.ManagementFacade
import io.quarkus.runtime.Startup
import me.kcybulski.api.MessageObserverFactory
import me.kcybulski.application.CommandsFacade
import me.kcybulski.application.StreamEvents
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.EventStoreConfiguration.eventStore
import me.kcybulski.ces.mongo.mongo
import me.kcybulski.ces.mongo.mongoManagementFacade
import javax.enterprise.context.ApplicationScoped
import javax.inject.Singleton

class AppConfiguration {

    @Singleton
    @Startup
    fun eventStore(): EventStore = eventStore {
        noSerialization()
        withEventsCache()
        mongo {
            mongoUrl = "mongodb+srv://quikommerce-server:O6TiWR45kXVVsQva@quikommerce.48gg0q0.mongodb.net/?w=2"
            database = "event-store"
        }
    }

    @Singleton
    @Startup
    fun management(eventStore: EventStore, objectMapper: ObjectMapper): ManagementFacade =
        mongoManagementFacade(eventStore, objectMapper) {
            mongoUrl = "mongodb+srv://quikommerce-server:O6TiWR45kXVVsQva@quikommerce.48gg0q0.mongodb.net/?w=2"
            database = "event-store"
        }


    @ApplicationScoped
    fun messageObserverFactory(
        streamEvents: StreamEvents,
        commandsFacade: CommandsFacade
    ) = MessageObserverFactory(streamEvents, commandsFacade)

//    @Singleton
//    @Startup
//    fun objectMapper(): ObjectMapper =
//        ObjectMapper()
//            .findAndRegisterModules()
}