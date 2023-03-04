package me.kcybulski.ces.api.aggregates

import me.kcybulski.ces.api.Event


abstract class Aggregate<T: Aggregate<T>> {

    internal val unpublishedEvents: MutableList<Event<*>> = mutableListOf()

    protected fun event(event: Event<*>) {
        unpublishedEvents += event
    }

    abstract fun apply(event: Event<*>): T

}