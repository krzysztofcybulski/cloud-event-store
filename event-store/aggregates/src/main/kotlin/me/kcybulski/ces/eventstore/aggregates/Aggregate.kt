package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.Stream


abstract class Aggregate<T : Aggregate<T>> {

    internal val unpublishedEvents: MutableList<Event<*>> = mutableListOf()

    abstract val stream: Stream

    protected fun event(event: Event<*>): T {
        unpublishedEvents += event
        return apply(event)
    }

    abstract fun apply(event: Event<*>): T

}