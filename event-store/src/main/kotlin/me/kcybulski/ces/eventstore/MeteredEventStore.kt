package me.kcybulski.ces.eventstore

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit.NANOSECONDS


class MeteredEventStore(
    private val eventStore: EventStore,
    private val meterRegistry: MeterRegistry
) : EventStore {

    val publishTimer = registerTimer("eventstore.publish.time")
    val readTimer = registerTimer("eventstore.read.time")
    val subscribeTimer = registerTimer("eventstore.subscribe.time")

    override suspend fun <A : Any> publish(
        event: Event<A>,
        stream: Stream,
        expectedSequenceNumber: ExpectedSequenceNumber
    ): PublishingResult =
        publishTimer.recordSuspend {
            eventStore.publish(event, stream, expectedSequenceNumber)
                .also { reportPublishingResult(it) }
        }

    override suspend fun read(readQuery: ReadQuery): EventStream =
        readTimer.recordSuspend {
            eventStore.read(readQuery)
        }

    override suspend fun <T : Any> subscribe(name: String, type: String, handler: suspend (StreamedEvent<T>) -> Unit) =
        subscribeTimer.recordSuspend {
            eventStore.subscribe(name, type, handler)
        }

    private fun reportPublishingResult(publishingResult: PublishingResult) =
        when (publishingResult) {
            is PublishingResult.Success -> meterRegistry.counter("eventstore.publish.success").increment()
            is PublishingResult.Failure -> meterRegistry.counter("eventstore.publish.failure").increment()
        }


    private fun registerTimer(name: String): Timer = Timer
        .builder(name)
        .publishPercentiles(0.5, 0.75, 0.95, 0.99)
        .register(meterRegistry)

}

private suspend fun <A> Timer.recordSuspend(block: suspend () -> A): A {
    val start = System.nanoTime()
    try {
        return block()
    } finally {
        record(System.nanoTime() - start, NANOSECONDS)
    }
}