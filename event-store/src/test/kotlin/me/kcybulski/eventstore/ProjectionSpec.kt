package me.kcybulski.eventstore

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import me.kcybulski.ces.api.EventStore
import me.kcybulski.ces.api.EventStoreConfiguration
import me.kcybulski.ces.api.ReadQuery.SpecificStream
import me.kcybulski.ces.api.Stream
import me.kcybulski.eventstore.testdata.AddProductEvent
import me.kcybulski.eventstore.testdata.hasShoppingCartStream

class ProjectionSpec : StringSpec({

    val eventStore: EventStore = EventStoreConfiguration.inMemoryEventStore()

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