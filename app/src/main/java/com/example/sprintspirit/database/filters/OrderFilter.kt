package com.example.sprintspirit.database.filters

enum class OrderFilter {
    NEW {
        override fun columnName() = "startTime"
    },
    DISTANCE {
        override fun columnName() = "distance"
    };

    abstract fun columnName(): String
}
