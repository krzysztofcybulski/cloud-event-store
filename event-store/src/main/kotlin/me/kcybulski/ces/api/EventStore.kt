package me.kcybulski.ces.api

import me.kcybulski.ces.api.ExpectedSequenceNumber.AnySequenceNumber
import me.kcybulski.ces.api.ExpectedSequenceNumber.SpecificSequenceNumber
import me.kcybulski.ces.api.Stream.Companion.GLOBAL

@JvmInline
value class Stream(val id: String) {

    companion object {

        internal val GLOBAL = Stream("__global__")
    }
}

interface EventStore {

    suspend fun <A : Any> publish(
        event: Event<A>,
        stream: Stream = GLOBAL,
        expectedSequenceNumber: ExpectedSequenceNumber = AnySequenceNumber
    ): PublishingResult

    suspend fun read(readQuery: ReadQuery): EventStream

    suspend fun <T : Any> subscribe(
        type: String,
        handler: suspend (T) -> Unit
    )

}

sealed interface PublishingResult {

    data class Success(val eventId: EventId) : PublishingResult

    sealed interface Failure : PublishingResult {

        data class InvalidExpectedSequenceNumber(val expectedSequenceNumber: SpecificSequenceNumber) : Failure

    }
}

sealed interface ReadQuery {

    data class SpecificStream(val stream: Stream) : ReadQuery
    data class SpecificEvent(val eventId: EventId) : ReadQuery
    object AllEvents : ReadQuery

}