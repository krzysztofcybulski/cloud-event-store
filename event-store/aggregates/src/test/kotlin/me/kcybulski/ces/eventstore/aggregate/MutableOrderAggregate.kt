package me.kcybulski.ces.eventstore.aggregate

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.SimpleEvent
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.aggregates.Aggregate
import me.kcybulski.ces.eventstore.aggregates.AggregateCreator
import java.util.UUID

class MutableOrderAggregate(
    val id: String,
    val products: MutableList<String>
) : Aggregate<MutableOrderAggregate>() {

    override val stream = Stream(id)

    fun addProduct(productName: String) {
        event(ProductAdded(id, productName))
    }

    override fun apply(event: Event<*>): MutableOrderAggregate {
        when (event) {
            is ProductAdded -> products += event.name
        }
        return this
    }

    companion object: AggregateCreator<MutableOrderAggregate, OrderCreated> {

        override fun from(event: OrderCreated): MutableOrderAggregate {
            return MutableOrderAggregate(event.id, mutableListOf())
        }

        fun createNew(): MutableOrderAggregate {
            val id = UUID.randomUUID().toString()
            return MutableOrderAggregate(
                id,
                mutableListOf()
            ).event(OrderCreated(id))
        }

    }
}

data class OrderCreated(val id: String) : SimpleEvent()

data class ProductAdded(
    val orderId: String,
    val name: String
) : SimpleEvent()