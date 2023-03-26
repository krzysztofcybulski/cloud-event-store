package me.kcybulski.ces.mongo

import com.quikommerce.ces.eventstore.management.StreamMetadata
import com.quikommerce.ces.eventstore.management.StreamsMetadataRepository
import kotlinx.coroutines.flow.Flow
import me.kcybulski.ces.eventstore.Stream
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.findOneAndUpdateUpsert
import org.litote.kmongo.id.StringId
import org.litote.kmongo.inc

internal class MongoStreamsMetadataRepository(
    database: CoroutineDatabase
) : StreamsMetadataRepository {

    private val streams = database.getCollection<StreamMetadata>("streams")

    override suspend fun updateStream(id: Stream): StreamMetadata {
        return streams.findOneAndUpdate(
            filter = StreamMetadata::id eq StringId(id.id),
            update = inc(StreamMetadata::size, 1),
            options = findOneAndUpdateUpsert()
        )!!
    }

    override suspend fun findStream(id: Stream): StreamMetadata? {
        return streams.findOneById(id.id)
    }

    override suspend fun findStreams(type: String?): Flow<StreamMetadata> {
        return streams
            .find()
//            .find(type?.let { StreamMetadata::type eq it } ?: EMPTY_BSON)
            .toFlow()
    }
}