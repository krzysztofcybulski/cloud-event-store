package me.kcybulski.ces.api

object EventStoreConfiguration {

    fun inMemoryEventStore(): EventStore =
        BaseEventStore(
            repository = InMemoryEventsRepository()
        )

}