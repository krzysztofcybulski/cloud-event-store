package me.kcybulski

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName


@ConfigMapping(prefix = "eventstore")
interface EventStoreConfig {

    @WithName("name")
    fun name(): String?

}