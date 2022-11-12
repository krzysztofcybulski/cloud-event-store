package me.kcybulski.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Struct
import com.google.protobuf.Timestamp
import com.google.protobuf.util.JsonFormat
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.kcybulski.application.CommandsFacade
import me.kcybulski.application.QueriesFacade
import me.kcybulski.application.QueriesFacade.Event
import me.kcybulski.ces.Command
import me.kcybulski.ces.Command.CommandsCase.PUBLISH
import me.kcybulski.ces.Events
import me.kcybulski.ces.Message
import me.kcybulski.ces.Message.MessageCase.COMMAND
import me.kcybulski.ces.Message.MessageCase.QUERY
import me.kcybulski.ces.Query
import me.kcybulski.ces.Response
import mu.KotlinLogging
import java.time.Instant

class MessageObserver(
    private val queries: QueriesFacade,
    private val commands: CommandsFacade,
    private val responseObserver: StreamObserver<Response>,
    private val scope: CoroutineScope
) : StreamObserver<Message> {

    private val logger = KotlinLogging.logger {}

    override fun onNext(request: Message) {
        scope.launch {
            when (request.messageCase) {
                QUERY ->
                    queries
                        .query(request.query.toQuery())
                        .onEach { responseObserver.onNext(response(it)) }
                        .collect()
                COMMAND -> when (request.command.commandsCase) {
                    PUBLISH -> commands.publishEvents(request.command.toPublishCommand())
                    else -> {}
                }
                else -> {}
            }
        }
    }

    override fun onError(t: Throwable) {
    }

    override fun onCompleted() {
    }


    private fun response(event: Event): Response = Response
        .newBuilder()
        .setEvent(
            Events
                .newBuilder()
                .addEvent(
                    me.kcybulski.ces.Event
                        .newBuilder()
                        .setId(event.id)
                        .setType(event.type)
                        .setStream(event.stream)
                        .setPayload(ProtoAny.pack(fromJson(event.payload)))
                        .build()
                )
        )
        .build()

}

val objectMapper = ObjectMapper()

fun fromJson(obj: Any): com.google.protobuf.Message {
    val builder = Struct.newBuilder()
    println(objectMapper.writeValueAsString(obj))
    JsonFormat.parser().ignoringUnknownFields().merge(objectMapper.writeValueAsString(obj), builder);
    return builder.build()
}

class MessageObserverFactory(
    private val queries: QueriesFacade = QueriesFacade(),
    private val commands: CommandsFacade = CommandsFacade(),
    private val scope: CoroutineScope = CoroutineScope(Default + SupervisorJob())
) {

    fun observerFor(responseObserver: StreamObserver<Response>) =
        MessageObserver(queries, commands, responseObserver, scope)

}

private fun Query.toQuery(): QueriesFacade.Query = QueriesFacade.Query(
    streamIds = eventStreams.idList.toSet()
)

private fun Command.toPublishCommand(): CommandsFacade.PublishEventsCommand = CommandsFacade.PublishEventsCommand(
    events = publish.eventList.map { event ->
        CommandsFacade.PublishEventsCommand.Event(
            stream = event.id,
            type = event.type,
            timestamp = event.timestamp.toInstant(),
            payload = event.payload
        )
    }.toList()
)

private fun Timestamp.toInstant() = Instant.ofEpochSecond(seconds, nanos.toLong())