package me.kcybulski.eventstore

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import me.kcybulski.ces.api.EventStore
import me.kcybulski.ces.api.EventStoreConfiguration.inMemoryEventStore
import me.kcybulski.ces.api.ExpectedSequenceNumber.SpecificSequenceNumber
import me.kcybulski.ces.api.PublishingResult.Failure.InvalidExpectedSequenceNumber
import me.kcybulski.ces.api.PublishingResult.Success
import me.kcybulski.ces.api.Stream
import me.kcybulski.eventstore.testdata.AddProductEvent
import me.kcybulski.eventstore.testdata.hasShoppingCartStream

class PublishingEventsSpec : StringSpec({

    val eventStore: EventStore = inMemoryEventStore()

    val myShoppingCart = "1001"
    val addMilkEvent = AddProductEvent(myShoppingCart, "Milk")

    "should publish event" {
        //when
        val result = eventStore.publish(addMilkEvent)

        //then
        result.shouldBeInstanceOf<Success>()
    }

    "should publish event with specific sequence number to empty stream" {
        //when
        val result = eventStore.publish(
            event = addMilkEvent,
            stream = Stream(myShoppingCart),
            expectedSequenceNumber = SpecificSequenceNumber(0)
        )

        //then
        result.shouldBeInstanceOf<Success>()
    }

    "should not publish event if given invalid expected sequence number to empty stream" {
        //when
        val result = eventStore.publish(
            event = addMilkEvent,
            stream = Stream(myShoppingCart),
            expectedSequenceNumber = SpecificSequenceNumber(1)
        )

        //then
        result.shouldBeInstanceOf<InvalidExpectedSequenceNumber>()
    }
    "should publish event with specific sequence number to stream with some events" {
        //given
        eventStore hasShoppingCartStream myShoppingCart

        //when
        val result = eventStore.publish(
            event = addMilkEvent,
            stream = Stream(myShoppingCart),
            expectedSequenceNumber = SpecificSequenceNumber(3)
        )

        //then
        result.shouldBeInstanceOf<Success>()
    }

    "should not publish event if given invalid expected sequence number to stream with some events" {
        //given
        eventStore hasShoppingCartStream myShoppingCart

        //when
        val result = eventStore.publish(
            event = addMilkEvent,
            stream = Stream(myShoppingCart),
            expectedSequenceNumber = SpecificSequenceNumber(2)
        )

        //then
        result.shouldBeInstanceOf<InvalidExpectedSequenceNumber>()
    }
})