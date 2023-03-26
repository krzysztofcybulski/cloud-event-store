package me.kcybulski.api

import com.google.protobuf.util.Timestamps
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import me.kcybulski.application.CommandsFacade
import me.kcybulski.application.StreamEvents
import me.kcybulski.ces.EventStream
import me.kcybulski.ces.ExpectedSequenceNumber.KindCase.ANY
import me.kcybulski.ces.ExpectedSequenceNumber.KindCase.SPECIFICSEQUENCENUMBER
import me.kcybulski.ces.Message
import me.kcybulski.ces.Message.MessageCase.MESSAGE_NOT_SET
import me.kcybulski.ces.Message.MessageCase.PUBLISH
import me.kcybulski.ces.Message.MessageCase.STREAMQUERY
import me.kcybulski.ces.PublishEvent
import me.kcybulski.ces.Response
import me.kcybulski.ces.Stream.KindCase.GLOBAL
import me.kcybulski.ces.Stream.KindCase.STREAMID
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.AnySequenceNumber
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.SpecificSequenceNumber
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.StreamedEvent
import mu.KotlinLogging.logger
import me.kcybulski.ces.ExpectedSequenceNumber.KindCase.KIND_NOT_SET as EXPECTED_SEQUENCE_KIND_NOT_SEND
import me.kcybulski.ces.Stream.KindCase.KIND_NOT_SET as STREAM_KIND_NOT_SET

class MessageObserver(
    private val queries: StreamEvents,
    private val commands: CommandsFacade,
    private val responseObserver: StreamObserver<Response>,
    private val scope: CoroutineScope
) : StreamObserver<Message> {

    private val logger = logger {}

    override fun onNext(request: Message) {
        scope.launch {
            when (request.messageCase) {
                STREAMQUERY ->
                    queries
                        .streamEvents(Stream(request.streamQuery.id))
                        .toList()
                        .let {
                            responseObserver.onNext(
                                response(it)
                            )
                        }

                PUBLISH ->
                    commands.publish(request.publish.toPublishCommand())

                MESSAGE_NOT_SET, null -> {
                    logger.warn { "Not message $request.messageCase" }
                }
            }
        }
    }

    override fun onError(t: Throwable) {
    }

    override fun onCompleted() {
    }


    private fun response(event: List<StreamedEvent<*>>): Response = Response
        .newBuilder()
        .setEventStream(
            EventStream
                .newBuilder()
                .addAllEvent(
                    event.map { event ->
                        me.kcybulski.ces.StreamedEvent
                            .newBuilder()
                            .setId(event.id.raw)
                            .setType(event.type)
                            .setPayload(event.payload.toString())
                            .setTimestamp(Timestamps.fromMillis(event.timestamp.toEpochMilli()))
                            .build()
                    }
                )
        )
        .build()
}

class MessageObserverFactory(
    private val queries: StreamEvents,
    private val commands: CommandsFacade,
    private val scope: CoroutineScope = CoroutineScope(Default + SupervisorJob())
) {

    fun observerFor(responseObserver: StreamObserver<Response>) =
        MessageObserver(queries, commands, responseObserver, scope)

}

private fun PublishEvent.toPublishCommand(): CommandsFacade.PublishEventCommand =
    CommandsFacade.PublishEventCommand(
        stream = when (stream.kindCase) {
            STREAMID -> Stream(stream.streamId)
            GLOBAL, STREAM_KIND_NOT_SET, null -> Stream.GLOBAL
        },
        type = type,
        payload = payload,
        expectedSequenceNumber = when (expectedSequenceNumber.kindCase) {
            SPECIFICSEQUENCENUMBER -> SpecificSequenceNumber(expectedSequenceNumber.specificSequenceNumber.toLong())
            ANY, EXPECTED_SEQUENCE_KIND_NOT_SEND, null -> AnySequenceNumber
        }
    )