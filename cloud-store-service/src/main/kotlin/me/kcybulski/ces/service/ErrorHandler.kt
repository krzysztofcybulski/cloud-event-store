package me.kcybulski.ces.service

import mu.KotlinLogging
import ratpack.core.error.ClientErrorHandler
import ratpack.core.error.ServerErrorHandler
import ratpack.core.handling.Context

class ErrorHandler : ClientErrorHandler, ServerErrorHandler {

    private val logger = KotlinLogging.logger {}

    override fun error(context: Context, statusCode: Int) {
        logger.warn { "Client error resulted in: $statusCode" }
    }

    override fun error(context: Context, throwable: Throwable?) {
        logger.error(throwable) { "Server error" }
    }
}