package me.kcybulski.ces.eventstore

@JvmInline
value class Stream(val id: String) {

    companion object {

        val GLOBAL = Stream("__global__")
    }
}