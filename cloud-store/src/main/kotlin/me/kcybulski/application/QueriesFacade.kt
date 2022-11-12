package me.kcybulski.application

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import java.util.UUID.randomUUID
import kotlin.time.Duration.Companion.seconds

class QueriesFacade {

    private val logger = KotlinLogging.logger {}

    fun query(query: Query): Flow<Event> {
        logger.info { "Query $query" }
        return flow {
            repeat(20) {
                emit(Event(randomUUID().toString(), "NUMBER", "1", mapOf("x" to it)))
                delay(5.seconds)
            }
        }
    }

    class Query(
        val streamIds: Set<String>
    )

    data class Event(
        val id: String,
        val type: String,
        val stream: String,
        val payload: Any
    )
}