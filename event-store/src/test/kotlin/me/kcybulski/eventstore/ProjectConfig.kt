package me.kcybulski.eventstore

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.IsolationMode.InstancePerLeaf

class ProjectConfig: AbstractProjectConfig() {

    override val isolationMode = InstancePerLeaf
}