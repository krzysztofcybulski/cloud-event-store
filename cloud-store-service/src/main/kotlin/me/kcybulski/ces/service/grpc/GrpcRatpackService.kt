package me.kcybulski.ces.service.grpc

import ratpack.core.service.Service
import ratpack.core.service.StartEvent
import ratpack.core.service.StopEvent

class GrpcRatpackService(
    private val grpcServer: GrpcServer
) : Service {

    override fun onStart(event: StartEvent) {
        grpcServer.start()
    }

    override fun onStop(event: StopEvent) {
        grpcServer.stop()
    }

}