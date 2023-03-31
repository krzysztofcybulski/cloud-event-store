package com.quikommerce.ces.eventstore.management

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import me.kcybulski.ces.eventstore.Stream
import org.litote.kmongo.id.StringId

interface StreamsMetadataRepository {

    suspend fun incrementStreamSize(id: Stream): StreamMetadata

    suspend fun findStream(id: Stream): StreamMetadata?

    suspend fun findStreams(type: String?): Flow<StreamMetadata>
}

class InMemoryStreamsMetadataRepository: StreamsMetadataRepository {

    private val streamSizes: MutableMap<Stream, Int> = mutableMapOf()

    override suspend fun incrementStreamSize(id: Stream): StreamMetadata {
        val newSize = streamSizes.merge(id, 1) { a, b -> a + b } ?: 0
        return StreamMetadata(StringId(id.id), newSize)
    }

    override suspend fun findStream(id: Stream): StreamMetadata? {
        return streamSizes[id]?.let { StreamMetadata(StringId(id.id), it) }
    }

    override suspend fun findStreams(type: String?): Flow<StreamMetadata> {
        return emptyFlow()
    }
}