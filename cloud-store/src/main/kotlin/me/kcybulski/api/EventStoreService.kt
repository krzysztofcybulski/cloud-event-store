package me.kcybulski.api

import io.grpc.stub.StreamObserver
import io.quarkus.grpc.GrpcService
import me.kcybulski.ces.Message
import me.kcybulski.ces.Response
import me.kcybulski.ces.StreamingGrpc

@GrpcService
class EventStoreService : StreamingGrpc.StreamingImplBase() {

    private val factory = MessageObserverFactory()

    override fun pipe(responseObserver: StreamObserver<Response>): StreamObserver<Message> =
        factory.observerFor(responseObserver)

}