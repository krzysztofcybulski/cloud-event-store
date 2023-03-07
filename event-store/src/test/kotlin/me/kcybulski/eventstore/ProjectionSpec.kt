package me.kcybulski.eventstore

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.EventStoreConfiguration
import me.kcybulski.ces.eventstore.EventStoreConfiguration.eventStore
import me.kcybulski.ces.eventstore.ReadQuery.SpecificStream
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.eventstore.testdata.AddProductEvent
import me.kcybulski.eventstore.testdata.hasShoppingCartStream

class ProjectionSpec : StringSpec({

    val eventStore: EventStore = eventStore { }

    val myShoppingCart = "1001"

    "should create new projection from event stream" {
        //given
        eventStore hasShoppingCartStream myShoppingCart

        //and
        val stream = eventStore.read(SpecificStream(Stream(myShoppingCart)))

        //when
        val projection = stream.project("") { agg, event ->
            (agg + (event as? AddProductEvent)?.productName?.firstOrNull())
        }

        //then
        projection shouldBe "CBW"
    }

})