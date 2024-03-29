package me.kcybulski.ces.mongo

import com.mongodb.ErrorCategory
import com.mongodb.ErrorCategory.DUPLICATE_KEY
import com.mongodb.MongoWriteException
import com.mongodb.client.model.IndexOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import me.kcybulski.ces.eventstore.EventId
import me.kcybulski.ces.eventstore.EventsRepository
import me.kcybulski.ces.eventstore.SaveEventResult
import me.kcybulski.ces.eventstore.SaveEventResult.OptimisticLockingError
import me.kcybulski.ces.eventstore.SaveEventResult.Saved
import me.kcybulski.ces.eventstore.SaveEventResult.SavingError
import me.kcybulski.ces.eventstore.SerializedEvent
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.tasks.TasksRepository
import mu.KLogging
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.ne
import org.litote.kmongo.pull
import java.time.Instant

internal class MongoEventsRepository(
    database: CoroutineDatabase
) : EventsRepository, TasksRepository {

    private val events = database.getCollection<MongoEvent>("events")

    init {
        runBlocking {
            logger.debug { "Ensuring that unique idnex (stream, sequenceNumber) exists" }
            events.ensureIndex(
                MongoEvent::stream,
                MongoEvent::sequenceNumber,
                indexOptions = IndexOptions().unique(true)
            )
        }
    }

    override suspend fun save(event: SerializedEvent): SaveEventResult {
        val lastSequenceNumber = findLastSequenceNumber(event)
        if (event.sequenceNumber != null && event.sequenceNumber != lastSequenceNumber + 1) {
            return OptimisticLockingError
        }
        try {
            tryToSave(event, lastSequenceNumber)
        } catch (e: MongoWriteException) {
            return handleSavingError(e)
        }
        return Saved
    }

    private suspend fun findLastSequenceNumber(event: SerializedEvent) =
        events
            .find(MongoEvent::stream eq event.stream.id)
            .descendingSort(MongoEvent::sequenceNumber)
            .first()
            ?.sequenceNumber
            ?: -1

    private suspend fun tryToSave(event: SerializedEvent, lastSequenceNumber: Long) {
        events.save(
            MongoEvent.from(
                event.copy(
                    sequenceNumber = event.sequenceNumber ?: (lastSequenceNumber + 1)
                )
            )
        )
    }

    private fun handleSavingError(e: MongoWriteException): SaveEventResult {
        if (ErrorCategory.fromErrorCode(e.error.code) == DUPLICATE_KEY) {
            return OptimisticLockingError
        }
        return SavingError(e)
    }

    override suspend fun loadAll(): Flow<SerializedEvent> =
        events.find().toFlow().map { it.asSerializedEvent() }

    override suspend fun loadStreamFrom(stream: Stream, from: Long): Flow<SerializedEvent> =
        events
            .find(
                and(
                    MongoEvent::stream eq stream.id,
                    MongoEvent::sequenceNumber gte from
                )
            )
            .toFlow()
            .map { it.asSerializedEvent() }

    override suspend fun findEvent(eventId: EventId): SerializedEvent? =
        events.findOne(MongoEvent::id eq eventId.raw)?.asSerializedEvent()

    override suspend fun processed(id: EventId, subscriberName: String) {
        events.updateOne(
            filter = MongoEvent::id eq id.raw,
            update = pull(MongoEvent::subscribers, subscriberName)
        )
    }

    override suspend fun findUnprocessedTask(): SerializedEvent? =
        events.findOne(MongoEvent::subscribers ne emptyList<Nothing>())?.asSerializedEvent()

    companion object : KLogging()
}

internal class MongoEvent(
    @BsonId
    val id: String,
    val stream: String,
    val type: String,
    val timestamp: String,
    val subscribers: List<String>,
    val sequenceNumber: Long?,
    val payload: String,
    val _class: String
) {

    fun asSerializedEvent() = SerializedEvent(
        id = EventId(id),
        stream = Stream(stream),
        type = type,
        timestamp = Instant.parse(timestamp),
        subscribers = subscribers.toMutableList(),
        sequenceNumber = sequenceNumber,
        payload = payload,
        _class = _class
    )

    companion object {

        fun from(serializedEvent: SerializedEvent) = MongoEvent(
            id = serializedEvent.id.raw,
            stream = serializedEvent.stream.id,
            type = serializedEvent.type,
            timestamp = serializedEvent.timestamp.toString(),
            subscribers = serializedEvent.subscribers,
            sequenceNumber = serializedEvent.sequenceNumber,
            payload = serializedEvent.payload,
            _class = serializedEvent._class
        )
    }

}