package me.kcybulski.ces.api

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper

interface EventSerializer {

    suspend fun <T> serialize(payload: T): String
    suspend fun <T> deserialize(serialized: String, clazz: Class<T>): T

}

internal class JsonEventSerializer(
    private val objectMapper: ObjectMapper
) : EventSerializer {

    override suspend fun <T> serialize(payload: T): String =
        objectMapper.writeValueAsString(payload)

    override suspend fun <T> deserialize(serialized: String, clazz: Class<T>): T =
        objectMapper.readValue(serialized, clazz)
}

internal abstract class IgnoreSimpleEventPayload {

    @get:JsonIgnore
    abstract val payload: Any
    @get:JsonIgnore
    abstract val type: Any

}