package me.kcybulski.ces.eventstore.aggregates

import me.kcybulski.ces.eventstore.Event
import me.kcybulski.ces.eventstore.Stream


abstract class Aggregate<T : Aggregate<T>> {

    internal var unpublishedEvents: List<Event<*>> = listOf()
    var version: Long = -1L

    abstract val stream: Stream

    protected fun event(event: Event<*>): T {
        return apply(event)
            .also { it.unpublishedEvents = unpublishedEvents + event }
            .also { it.version = version }
    }

    abstract fun apply(event: Event<*>): T

}