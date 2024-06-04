package com.example.sprintspirit.database.filters

enum class OrderFilter {
    NEW {
        override fun columnName() = "publishDate"
    },
    TIME {
        override fun columnName() = "minutes"
    },
    DISTANCE {
        override fun columnName() = "distance"
    };

    abstract fun columnName(): String
}
