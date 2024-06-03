package com.example.sprintspirit.features.run.location

enum class LocationRefreshRate {

    LOWEST{
        override fun getMilli() = 15000L
    },
    LOW{
        override fun getMilli() = 10000L
    },
    NORMAL{
        override fun getMilli() = 5000L
    },
    HIGH{
        override fun getMilli() = 2000L
    },
    HIGHEST{
        override fun getMilli() = 1000L
    };

    abstract fun getMilli(): Long

    companion object {
        fun fromMilliseconds(milliseconds: Long): LocationRefreshRate {
            return when {
                milliseconds == HIGHEST.getMilli() -> HIGHEST
                milliseconds == HIGH.getMilli() -> HIGH
                milliseconds == NORMAL.getMilli() -> NORMAL
                milliseconds == LOW.getMilli() -> LOW
                else -> NORMAL
            }
        }
    }

}