package me.kcybulski.eventstore

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.EventStoreConfiguration
import me.kcybulski.ces.eventstore.EventStoreConfiguration.eventStore
import me.kcybulski.ces.eventstore.EventStream
import me.kcybulski.ces.eventstore.ReadQuery.AllEvents
import me.kcybulski.ces.eventstore.ReadQuery.SpecificEvent
import me.kcybulski.ces.eventstore.ReadQuery.SpecificStream
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.eventstore.testdata.AddProductEvent
import me.kcybulski.eventstore.testdata.hasShoppingCartStream

class ReadingEventsSpec : StringSpec({

    val eventStore: EventStore = eventStore { }

    val myShoppingCart = "1001"

    "should read event stream" {
        //given
        eventStore hasShoppingCartStream myShoppingCart

        //when
        val stream = eventStore.read(SpecificStream(Stream(myShoppingCart)))

        //then
        stream.productNames() shouldBe listOf("Chocolate", "Butter", "Water")
    }

    "should read empty event stream" {
        //when
        val stream = eventStore.read(SpecificStream(Stream(myShoppingCart)))

        //then
        stream.collectList() shouldBe listOf()
    }

    "should read event stream starting from a given event" {
        //given
        eventStore hasShoppingCartStream myShoppingCart
        //and
        val butterEventId = eventStore.butterEventId(myShoppingCart)

        //when
        val stream = eventStore
            .read(SpecificStream(Stream(myShoppingCart)))
            .startingFrom(butterEventId)

        //then
        stream.productNames() shouldBe listOf("Butter", "Water")
    }

    "should read event stream backwards" {
        //given
        eventStore hasShoppingCartStream myShoppingCart

        //when
        val stream = eventStore
            .read(SpecificStream(Stream(myShoppingCart)))
            .backwards()

        //then
        stream.productNames() shouldBe listOf("Water", "Butter", "Chocolate")
    }

    "should read all events" {
        //given
        eventStore hasShoppingCartStream myShoppingCart
        eventStore hasShoppingCartStream "1002"

        //when
        val stream = eventStore.read(AllEvents)

        //then
        stream.collectList() shouldHaveSize 6
    }

    "should read specific event" {
        //given
        eventStore hasShoppingCartStream myShoppingCart
        //and
        val butterId = eventStore.butterEventId(myShoppingCart)


        //when
        val stream = eventStore.read(SpecificEvent(butterId))

        //then
        stream.productNames() shouldBe listOf("Butter")
    }
})

private suspend fun EventStream.productNames() =
    collectList()
        .map { it.payload }
        .mapNotNull { it as? AddProductEvent }
        .map { it.productName }

private suspend fun EventStore.butterEventId(shoppingCartId: String) =
    read(SpecificStream(Stream(shoppingCartId))).collectList()[1].id