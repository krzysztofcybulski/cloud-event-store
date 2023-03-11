package me.kcybulski.ces.eventstore.aggregate

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.aggregates.Aggregate
import me.kcybulski.ces.eventstore.aggregates.AggregateCreator
import java.util.UUID

class ImmutableOrderAggregate(
    val id: String,
    val products: List<String>
) : Aggregate<ImmutableOrderAggregate>() {

    override val stream = Stream(id)

    fun addProduct(productName: String): ImmutableOrderAggregate {
        return event(ProductAdded(id, productName))
    }

    override fun apply(event: Event<*>): ImmutableOrderAggregate =
        when (event) {
            is ProductAdded -> ImmutableOrderAggregate(id, products + event.name)
            else -> this
        }

    companion object: AggregateCreator<ImmutableOrderAggregate, OrderCreated> {

        override fun from(event: OrderCreated) = ImmutableOrderAggregate(event.id, mutableListOf())

        fun createNew(): ImmutableOrderAggregate {
            val id = UUID.randomUUID().toString()
            return ImmutableOrderAggregate(id, listOf()).event(OrderCreated(id))
        }
    }
}