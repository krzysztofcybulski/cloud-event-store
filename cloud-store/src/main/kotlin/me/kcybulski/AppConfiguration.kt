package me.kcybulski

import me.kcybulski.api.MessageObserverFactory
import me.kcybulski.application.CommandsFacade
import me.kcybulski.application.QueriesFacade
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.EventStoreConfiguration.eventStore
import javax.enterprise.context.ApplicationScoped

class AppConfiguration {

    @ApplicationScoped
    fun eventStore(): EventStore = eventStore {
        noSerialization()
    }


    @ApplicationScoped
    fun messageObserverFactory(
        queriesFacade: QueriesFacade,
        commandsFacade: CommandsFacade
    ) = MessageObserverFactory(queriesFacade, commandsFacade)

}