package com.example.sprintspirit.database.filters

enum class TimeFilter {
    WEEKLY{
        override fun timeMillis(): Long = 604800000
    },
    YEARLY{
        override fun timeMillis(): Long = 2629746000
    };

    abstract fun timeMillis(): Long
}