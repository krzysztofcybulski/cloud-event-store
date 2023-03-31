package me.kcybulski.ces.service.grpc

import java.net.InetSocketAddress

data class GrpcConfiguration(
    val port: Int,
    val reflection: Boolean
) {

    fun inetAddress() = InetSocketAddress(port)

}