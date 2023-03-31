package me.kcybulski.ces.client

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import me.kcybulski.ces.client.CloudStoreClientConfiguration.cloudStoreClient
import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.SimpleEvent
import me.kcybulski.ces.eventstore.Stream
import me.kcybulski.ces.eventstore.aggregates.Aggregate
import me.kcybulski.ces.eventstore.aggregates.AggregateCreator
import me.kcybulski.ces.eventstore.aggregates.Aggregates
import java.util.UUID

object CloudStoreClientConfiguration {

    fun cloudStoreClient(address: String): EventStore {
        val managedChannel = ManagedChannelBuilder
            .forTarget(address)
            .usePlaintext()
            .build()
        return CloudStoreClient(
            managedChannel,
            jacksonObjectMapper()
                .addMixIn(Event::class.java, IgnoreSimpleEventPayload::class.java)
        )
    }

}

internal abstract class IgnoreSimpleEventPayload {

    @get:JsonIgnore
    abstract val payload: Any

    @get:JsonIgnore
    abstract val type: Any

}

fun main() {

    val eventStore: EventStore = cloudStoreClient("localhost:5000")
    val aggregates = Aggregates.onEventStore(eventStore)

    val myOrder = Order
        .createNew()
        .addProduct("Milk")
        .addProduct("Butter")
        .addProduct("Sugar")

    runBlocking {
        aggregates.save(myOrder)
    }

    println("Saved")

    runBlocking {
        val loaded = aggregates.load<Order>(myOrder.stream)
        println(loaded?.products)
    }
}

data class Order(
    val id: String,
    val products: List<String>
) : Aggregate<Order>() {

    override val stream: Stream = Stream("order:$id")

    fun addProduct(name: String) = event(AddProduct(name))

    override fun apply(event: Event<*>): Order {
        return when (event) {
            is AddProduct -> Order(id, products + event.name)
            else -> this
        }
    }

    companion object : AggregateCreator<Order, CreateOrder> {
        override fun from(event: CreateOrder): Order = Order(event.id, emptyList())

        fun createNew(): Order {
            val id = UUID.randomUUID().toString()
            return Order(id, listOf()).event(CreateOrder(id))
        }

    }

}

data class CreateOrder(val id: String) : SimpleEvent()

data class AddProduct(val name: String) : SimpleEvent()