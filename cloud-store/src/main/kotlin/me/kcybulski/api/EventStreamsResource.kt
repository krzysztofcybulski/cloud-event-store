package me.kcybulski.api

import com.quikommerce.ces.eventstore.management.ManagementFacade
import com.quikommerce.ces.eventstore.management.StreamMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import me.kcybulski.ces.eventstore.Stream
import org.jboss.resteasy.reactive.RestQuery
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Response


@Path("/streams")
class EventStreamsResource(
    private val management: ManagementFacade
) {

    @GET
    suspend fun readEventStreams(
        @RestQuery id: String?,
        @RestQuery type: String?,
        @RestQuery page: Int = 0,
        @RestQuery pageSize: Int = 20
    ): Response =
        findStreams(id, type)
            .drop(page * pageSize)
            .take(pageSize)
            .let { toResponse(page, pageSize, it.toList()) }

    private suspend fun findStreams(id: String?, type: String?): Flow<StreamMetadata> =
        when {
            id != null -> management.findStream(Stream(id))?.let(::flowOf) ?: emptyFlow()
            else -> management.findStreams(type)
        }

    private fun toResponse(page: Int, pageSize: Int, streams: List<StreamMetadata>) =
        Response.ok(
            EventsStreamsPage(
                page = page,
                pageSize = pageSize,
                streams = streams
            )
        ).build()

    class EventsStreamsPage(
        val page: Int,
        val pageSize: Int,
        val streams: List<StreamMetadata>
    )

}