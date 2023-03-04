package me.kcybulski.eventstore

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe
import me.kcybulski.ces.api.EventStore
import me.kcybulski.ces.api.EventStoreConfiguration
import me.kcybulski.eventstore.testdata.AddProductEvent
import kotlin.time.Duration.Companion.seconds

class SubscribingToEventsSpec : StringSpec({

    val eventStore: EventStore = EventStoreConfiguration.inMemoryEventStore()

    val myShoppingCart = "1001"

    "should subscribe to specific event type" {
        //given
        val consumedProducts = mutableListOf<String>()

        //and
        eventStore.subscribe<AddProductEvent>("sub-1", "AddProductEvent")
        { productAdded -> consumedProducts += productAdded.productName }

        //when
        eventStore.publish(AddProductEvent(myShoppingCart, "Milk"))

        //then
        eventually(1.seconds) {
            consumedProducts shouldHaveSingleElement "Milk"
        }
    }

    "should retry subscriber if it failed" {
        //given
        var errorsCounter = 0
        val consumedProducts = mutableListOf<String>()

        //and
        eventStore.subscribe<AddProductEvent>("sub-1", "AddProductEvent")
        { productAdded -> consumedProducts += productAdded.productName }

        //and
        eventStore.subscribe<AddProductEvent>("sub-2", "AddProductEvent")
        { productAdded ->
            if (errorsCounter < 3) {
                errorsCounter++
                throw IllegalStateException("Error while handling")
            } else {
                consumedProducts += productAdded.productName
            }
        }

        //when
        eventStore.publish(AddProductEvent(myShoppingCart, "Milk"))

        //then
        eventually(1.seconds) {
            errorsCounter shouldBe 3
            consumedProducts shouldBe listOf("Milk", "Milk")
        }
    }

})