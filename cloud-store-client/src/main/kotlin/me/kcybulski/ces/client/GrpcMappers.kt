package me.kcybulski.ces.client

import com.fasterxml.jackson.databind.ObjectMapper
import me.kcybulski.ces.PublishCommandKt
import me.kcybulski.ces.PublishCommandKt.PublishEventKt.expectedSequenceNumber
import me.kcybulski.ces.PublishCommandKt.publishEvent
import me.kcybulski.ces.PublishResult
import me.kcybulski.ces.StreamMessage
import me.kcybulski.ces.StreamMessageKt
import me.kcybulski.ces.StreamedEvent
import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventId
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.AnySequenceNumber
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.SpecificSequenceNumber
import me.kcybulski.ces.eventstore.PublishingResult
import me.kcybulski.ces.eventstore.ReadQuery
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.publishCommand
import me.kcybulski.ces.streamMessage
import java.time.Instant

class GrpcMappers(private val objectMapper: ObjectMapper) {

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
        PublishingResult.Success(EventId(result.success.eventId))

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

    private fun streamRequest(eventStream: Stream) = PublishCommandKt.PublishEventKt.stream {
        if (eventStream == Stream.GLOBAL) {
            global = true
        } else {
            streamId = eventStream.id
        }
    }

}