package me.kcybulski.ces.api

interface Event<T> {

    val type: String
    val payload: T

}

abstract class SimpleEvent : Event<SimpleEvent> {

    override val type = this::class.simpleName ?: error("Cannot create simple event from anonymous class")

    override val payload by lazy { this }
}