package me.kcybulski.ces.service.grpc

import ratpack.core.impose.Impositions
import ratpack.core.server.RatpackServer
import ratpack.core.server.RatpackServerSpec
import ratpack.func.Action


interface GrpcRatpackServer : RatpackServer {

    companion object {
        fun of(definition: Action<in RatpackServerSpec>): GrpcRatpackServer =
            DefaultGrpcRatpackServer(definition, Impositions.current())

        fun start(definition: Action<in RatpackServerSpec>): GrpcRatpackServer =
            of(definition).also { it.start() }
    }
}