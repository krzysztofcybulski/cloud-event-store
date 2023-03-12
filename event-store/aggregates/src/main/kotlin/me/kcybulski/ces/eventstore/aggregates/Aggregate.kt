package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.Stream


abstract class Aggregate<T : Aggregate<T>> {

    internal var unpublishedEvents: List<Event<*>> = listOf()

    abstract val stream: Stream

    protected fun event(event: Event<*>): T {
        return apply(event)
            .also { it.unpublishedEvents = unpublishedEvents + event }
    }

    abstract fun apply(event: Event<*>): T

}