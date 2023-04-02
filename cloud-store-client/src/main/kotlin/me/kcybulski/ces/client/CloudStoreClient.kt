package me.kcybulski.ces.client

import io.grpc.ManagedChannel
import kotlinx.coroutines.flow.map
import me.kcybulski.ces.EventStoreGrpcKt.EventStoreCoroutineStub
import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.EventStream
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber
import me.kcybulski.ces.eventstore.PublishingResult
import me.kcybulski.ces.eventstore.ReadQuery
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.StreamedEvent
import mu.KLogging

internal class CloudStoreClient(
    managedChannel: ManagedChannel,
    private val mapper: GrpcMappers
) : EventStore {


    private val streamingStub = EventStoreCoroutineStub(managedChannel)

    override suspend fun <A : Any> publish(
        event: Event<A>,
        stream: Stream,
        expectedSequenceNumber: ExpectedSequenceNumber
    ): PublishingResult {
        logger.info { "Publishing ${event.type} event to cloud to stream $stream (expected version $expectedSequenceNumber)" }
        return mapper.publishCommandRequest(stream, expectedSequenceNumber, event)
            .let { streamingStub.publish(it) }
            .let(mapper::publishResult)
    }

    override suspend fun read(readQuery: ReadQuery): EventStream {
        logger.info { "Reading events $readQuery" }
        return streamingStub.stream(mapper.request(readQuery))
            .map(mapper::streamedEvent)
            .let(EventStream::from)
    }

    override suspend fun <T : Any> subscribe(name: String, type: String, handler: suspend (StreamedEvent<T>) -> Unit) {
        TODO("Not yet implemented")
    }

    companion object: KLogging()
}