package me.kcybulski.ces.eventstore

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper

interface EventSerializer {

    suspend fun <T> serialize(payload: T): String
    suspend fun deserialize(serialized: String, className: String): Any

}

internal class NoopSerializer(
) : EventSerializer {
    override suspend fun <T> serialize(payload: T): String = payload.toString()

    override suspend fun deserialize(serialized: String, className: String): Any = serialized

}

internal class JsonEventSerializer(
    private val objectMapper: ObjectMapper
) : EventSerializer {

    init {
        objectMapper.addMixIn(Event::class.java, IgnoreSimpleEventPayload::class.java)
    }

    override suspend fun <T> serialize(payload: T): String =
        objectMapper.writeValueAsString(payload)

    override suspend fun deserialize(serialized: String, className: String): Any =
        objectMapper.readValue(serialized, Class.forName(className)) as Any
}

internal abstract class IgnoreSimpleEventPayload {

    @get:JsonIgnore
    abstract val payload: Any

    @get:JsonIgnore
    abstract val type: Any

}