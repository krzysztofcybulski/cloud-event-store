package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.Event

interface AggregateCreator<T : Aggregate<T>, E : Event<*>> {

    fun from(event: E): T

}