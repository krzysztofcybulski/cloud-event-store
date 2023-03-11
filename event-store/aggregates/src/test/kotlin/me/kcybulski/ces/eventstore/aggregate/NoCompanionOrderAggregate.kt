package me.kcybulski.ces.eventstore.aggregate

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.aggregates.Aggregate
import java.util.UUID

class NoCompanionOrderAggregate @JvmOverloads constructor(
    var id: String = ""
) : Aggregate<NoCompanionOrderAggregate>() {

//    var id: String = ""
    var products: MutableList<String> = mutableListOf()

    override val stream = Stream(id)

    fun addProduct(productName: String): NoCompanionOrderAggregate {
        return event(ProductAdded(id, productName))
    }

    override fun apply(event: Event<*>): NoCompanionOrderAggregate {
        when (event) {
            is OrderCreated -> id = event.id
            is ProductAdded -> products += event.name
        }
        return this
    }

    companion object {

        fun createNew() =
            NoCompanionOrderAggregate()
                .event(OrderCreated(UUID.randomUUID().toString()))
    }
}