package com.quikommerce.ces.eventstore.management

import kotlinx.coroutines.flow.Flow
import me.kcybulski.ces.eventstore.Stream

interface StreamsMetadataRepository {

    suspend fun updateStream(id: Stream): StreamMetadata

    suspend fun findStream(id: Stream): StreamMetadata?

    suspend fun findStreams(type: String?): Flow<StreamMetadata>
}