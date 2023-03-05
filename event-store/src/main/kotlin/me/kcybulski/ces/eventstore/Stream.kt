package me.kcybulski.ces.eventstore

@JvmInline
value class Stream(val id: String) {

    companion object {

        internal val GLOBAL = Stream("__global__")
    }
}