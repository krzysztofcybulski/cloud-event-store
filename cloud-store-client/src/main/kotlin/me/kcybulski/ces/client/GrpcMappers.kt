package me.kcybulski.ces.client

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import me.kcybulski.ces.PublishCommandKt.PublishEventKt.expectedSequenceNumber
import me.kcybulski.ces.PublishCommandKt.PublishEventKt.stream
import me.kcybulski.ces.PublishCommandKt.publishEvent
import me.kcybulski.ces.PublishResult
import me.kcybulski.ces.PublishResult.PublishingError.ErrorsCase.ERRORS_NOT_SET
import me.kcybulski.ces.PublishResult.PublishingError.ErrorsCase.INTERNALERROR
import me.kcybulski.ces.PublishResult.PublishingError.ErrorsCase.INVALIDSEQUENCENUMBER
import me.kcybulski.ces.PublishResult.ResultsCase.ERROR
import me.kcybulski.ces.PublishResult.ResultsCase.RESULTS_NOT_SET
import me.kcybulski.ces.PublishResult.ResultsCase.SUCCESS
import me.kcybulski.ces.StreamMessage
import me.kcybulski.ces.StreamMessageKt
import me.kcybulski.ces.StreamedEvent
import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventId
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.AnySequenceNumber
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.SpecificSequenceNumber
import me.kcybulski.ces.eventstore.PublishingResult.Failure.InternalError
import me.kcybulski.ces.eventstore.PublishingResult.Failure.InvalidExpectedSequenceNumber
import me.kcybulski.ces.eventstore.PublishingResult.Success
import me.kcybulski.ces.eventstore.ReadQuery
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.publishCommand
import me.kcybulski.ces.streamMessage
import java.time.Instant

internal class GrpcMappers(private val objectMapper: ObjectMapper) {

    init {
        objectMapper.addMixIn(Event::class.java, IgnoreSimpleEventPayload::class.java)
    }

    fun streamedEvent(event: StreamedEvent) =
        me.kcybulski.ces.eventstore.StreamedEvent(
            id = EventId(event.id),
            timestamp = Instant.ofEpochSecond(event.timestamp.seconds, event.timestamp.nanos.toLong()),
            stream = Stream(event.streamId),
            type = event.type,
            className = event.className,
            payload = objectMapper.readValue(event.payload, Class.forName(event.className))
        )

    fun request(readQuery: ReadQuery): StreamMessage {
        require(readQuery is ReadQuery.SpecificStream)
        return streamMessage {
            this.streamQuery = StreamMessageKt.eventStreamQuery {
                this.id = readQuery.stream.id
            }
        }
    }

    fun <A : Any> publishCommandRequest(
        eventStream: Stream,
        eventExpectedSequenceNumber: ExpectedSequenceNumber,
        event: Event<A>
    ) = publishCommand {
        publish = publishEvent {
            stream = streamRequest(eventStream)
            expectedSequenceNumber = expectedSequenceNumberRequest(eventExpectedSequenceNumber)
            type = event.type
            className = event.className
            payload = objectMapper.writeValueAsString(event.payload)
        }
    }

    fun publishResult(result: PublishResult) =
        when (result.resultsCase) {
            SUCCESS -> Success(EventId(result.success.eventId))
            ERROR -> when (result.error.errorsCase) {
                INVALIDSEQUENCENUMBER -> InvalidExpectedSequenceNumber(SpecificSequenceNumber(result.error.invalidSequenceNumber.sequenceNumber.toLong()))
                INTERNALERROR, ERRORS_NOT_SET, null -> InternalError(IllegalStateException())
            }

            RESULTS_NOT_SET, null -> InternalError(IllegalStateException())
        }

    private fun expectedSequenceNumberRequest(eventExpectedSequenceNumber: ExpectedSequenceNumber) =
        expectedSequenceNumber {
            when (eventExpectedSequenceNumber) {
                AnySequenceNumber -> {
                    any = true
                }

                is SpecificSequenceNumber -> {
                    specificSequenceNumber = eventExpectedSequenceNumber.number.toInt()
                }
            }
        }

    private fun streamRequest(eventStream: Stream) =
        stream {
            if (eventStream == Stream.GLOBAL) {
                global = true
            } else {
                streamId = eventStream.id
            }
        }
}


internal abstract class IgnoreSimpleEventPayload {

    @get:JsonIgnore
    abstract val payload: Any

    @get:JsonIgnore
    abstract val type: Any

    @get:JsonIgnore
    abstract val className: Any

}