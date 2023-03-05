package me.kcybulski.ces.api

interface TasksRepository {

    suspend fun processed(id: EventId, subscriberName: String)
    suspend fun findUnprocessedTask(): SerializedEvent?

}
