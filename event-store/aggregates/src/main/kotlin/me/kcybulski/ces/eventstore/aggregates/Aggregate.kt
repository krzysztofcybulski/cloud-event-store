package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.Event


abstract class Aggregate<T: Aggregate<T>> {

    internal val unpublishedEvents: MutableList<Event<*>> = mutableListOf()

    protected fun event(event: Event<*>) {
        unpublishedEvents += event
    }

    abstract fun apply(event: Event<*>): T

}