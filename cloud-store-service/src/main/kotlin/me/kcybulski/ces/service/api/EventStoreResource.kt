package me.kcybulski.ces.service.api

import com.google.protobuf.util.Timestamps
import io.grpc.Status
import io.grpc.Status.UNIMPLEMENTED
import io.grpc.StatusException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.kcybulski.ces.EventStoreGrpcKt.EventStoreCoroutineImplBase
import me.kcybulski.ces.PublishCommand
import me.kcybulski.ces.PublishCommand.CommandCase.APPEND
import me.kcybulski.ces.PublishCommand.CommandCase.PUBLISH
import me.kcybulski.ces.PublishCommand.PublishEvent
import me.kcybulski.ces.PublishCommand.PublishEvent.ExpectedSequenceNumber.KindCase.SPECIFICSEQUENCENUMBER
import me.kcybulski.ces.PublishCommand.PublishEvent.Stream.KindCase.STREAMID
import me.kcybulski.ces.PublishResult
import me.kcybulski.ces.PublishResultKt.publishedSuccessfully
import me.kcybulski.ces.StreamMessage
import me.kcybulski.ces.StreamedEvent
import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber
import me.kcybulski.ces.eventstore.PublishingResult
import me.kcybulski.ces.eventstore.ReadQuery.SpecificStream
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.publishResult
import me.kcybulski.ces.streamedEvent
import mu.KLogging


class EventStoreResource(
    private val eventStore: EventStore
) : EventStoreCoroutineImplBase() {

    override suspend fun publish(request: PublishCommand): PublishResult =
        when (request.commandCase) {
            PUBLISH -> eventStore.publish(
                event = CloudEvent.fromGrpc(request.publish),
                stream = buildStream(request.publish.stream),
                expectedSequenceNumber = buildExpectedSequenceNumber(request.publish.expectedSequenceNumber)
            )

            APPEND -> {
                logger.warn { "Append is not yet implemented" }
                throw StatusException(UNIMPLEMENTED)
            }

            else -> throw StatusException(Status.INVALID_ARGUMENT)
        }
            .let { result ->
                publishResult {
                    success = publishedSuccessfully {
                        eventId = (result as? PublishingResult.Success)?.eventId?.raw ?: ""
                    }
                }
            }

    override fun stream(request: StreamMessage): Flow<StreamedEvent> = flow {
        eventStore.read(SpecificStream(Stream(request.streamQuery.id)))
            .flow()
            .map { event ->
                streamedEvent {
                    id = event.id.raw
                    type = event.type
                    streamId = event.stream.id
                    timestamp = Timestamps.fromMillis(event.timestamp.toEpochMilli())
                    className = event.className
                    payload = event.payload as String
                }
            }
            .collect { emit(it) }
    }

    private fun buildStream(stream: PublishEvent.Stream) =
        when (stream.kindCase) {
            STREAMID -> Stream(stream.streamId)
            else -> Stream.GLOBAL
        }

    private fun buildExpectedSequenceNumber(expectedSequenceNumber: PublishEvent.ExpectedSequenceNumber) =
        when (expectedSequenceNumber.kindCase) {
            SPECIFICSEQUENCENUMBER -> ExpectedSequenceNumber.SpecificSequenceNumber(expectedSequenceNumber.specificSequenceNumber.toLong())
            else -> ExpectedSequenceNumber.AnySequenceNumber
        }

    companion object : KLogging()
}

class CloudEvent(
    override val type: String,
    override val payload: String,
    override val className: String
) : Event<String> {

    companion object {

        fun fromGrpc(publish: PublishEvent) =
            CloudEvent(
                type = publish.type,
                payload = publish.payload,
                className = publish.className
            )

    }
}