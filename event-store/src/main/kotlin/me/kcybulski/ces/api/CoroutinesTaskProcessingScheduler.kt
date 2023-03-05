package me.kcybulski.ces.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.time.Duration.Companion.milliseconds

internal class CoroutinesTaskProcessingScheduler(
    private val tasksProcessor: TasksProcessor,
    private val coroutine: CoroutineScope = CoroutineScope(newSingleThreadContext("task-processing"))
) {

    private lateinit var job: Job

    suspend fun start() {
        this.job = coroutine.launch {
            while (true) {
                tasksProcessor.processNext()
                delay(20.milliseconds)
            }
        }
    }

    suspend fun stop() {
        this.job.children.forEach { it.cancelAndJoin() }
    }

}