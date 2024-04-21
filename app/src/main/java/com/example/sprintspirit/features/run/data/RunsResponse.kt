package com.example.sprintspirit.features.run.data

class RunsResponse {
    var runs: List<RunData>? = null
    var exception: Exception? = null

    fun runsByTime(): List<RunData>? {
        return runs?.sortedBy { it.startTime }
    }
}