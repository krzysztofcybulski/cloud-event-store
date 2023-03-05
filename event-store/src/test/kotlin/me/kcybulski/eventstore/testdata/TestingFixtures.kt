package me.kcybulski.eventstore.testdata

import me.kcybulski.ces.eventstore.EventStore
import me.kcybulski.ces.eventstore.EventStream
import me.kcybulski.ces.eventstore.ReadQuery.SpecificStream
import me.kcybulski.ces.eventstore.Stream

suspend infix fun EventStore.hasShoppingCartStream(shoppingCartId: String): EventStream {
    val stream = Stream(shoppingCartId)
    publish(AddProductEvent(shoppingCartId, "Chocolate"), stream)
    publish(AddProductEvent(shoppingCartId, "Butter"), stream)
    publish(AddProductEvent(shoppingCartId, "Water"), stream)
    return read(SpecificStream(stream))
}