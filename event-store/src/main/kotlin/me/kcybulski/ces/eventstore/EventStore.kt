package me.kcybulski.ces.eventstore

import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.AnySequenceNumber
import me.kcybulski.ces.eventstore.ExpectedSequenceNumber.SpecificSequenceNumber
import me.kcybulski.ces.eventstore.Stream.Companion.GLOBAL

interface EventStore {

    suspend fun <A : Any> publish(
        event: Event<A>,
        stream: Stream = GLOBAL,
        expectedSequenceNumber: ExpectedSequenceNumber = AnySequenceNumber
    ): PublishingResult

    suspend fun read(readQuery: ReadQuery): EventStream

    suspend fun <T: Any> subscribe(
        name: String,
        type: String,
        handler: suspend (StreamedEvent<T>) -> Unit
    )

}

sealed interface PublishingResult {

    data class Success(val eventId: EventId) : PublishingResult

    sealed interface Failure : PublishingResult {

        data class InvalidExpectedSequenceNumber(val expectedSequenceNumber: SpecificSequenceNumber) : Failure
        data class InternalError(val cause: Throwable): Failure

    }
}

sealed interface ReadQuery {

    data class SpecificStream(val stream: Stream) : ReadQuery
    data class SpecificEvent(val eventId: EventId) : ReadQuery
    object AllEvents : ReadQuery

}