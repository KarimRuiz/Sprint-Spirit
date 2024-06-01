package com.example.sprintspirit.database.filters

enum class TimeFilter {
    DAILY{
        override fun timeMillis(): Long = 86400000
    },
    WEEKLY{
        override fun timeMillis(): Long = 604800000
    },
    YEARLY{
        override fun timeMillis(): Long = 2629746000
    };

    abstract fun timeMillis(): Long
}