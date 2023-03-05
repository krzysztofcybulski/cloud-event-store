package me.kcybulski.ces.eventstore.tasks

import me.kcybulski.ces.eventstore.EventId
import me.kcybulski.ces.eventstore.SerializedEvent

interface TasksRepository {

    suspend fun processed(id: EventId, subscriberName: String)
    suspend fun findUnprocessedTask(): SerializedEvent?

}
