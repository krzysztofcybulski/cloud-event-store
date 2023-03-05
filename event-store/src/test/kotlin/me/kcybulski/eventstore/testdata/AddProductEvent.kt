package me.kcybulski.eventstore.testdata

import me.kcybulski.ces.eventstore.SimpleEvent

class AddProductEvent(
    val shoppingCartId: String,
    val productName: String
) : SimpleEvent()