package me.kcybulski.ces.eventstore

interface Event<T> {

    val type: String
    val payload: T
    val className: String

}

abstract class SimpleEvent : Event<SimpleEvent> {

    override val type = this::class.simpleName ?: error("Cannot create simple event from anonymous class")

    override val payload by lazy { this }

    override val className = this::class.java.name
}