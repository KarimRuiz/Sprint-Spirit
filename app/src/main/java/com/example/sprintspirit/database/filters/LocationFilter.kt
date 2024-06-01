package com.example.sprintspirit.database.filters

enum class LocationFilter {

    EMPTY{
        override fun toFieldName() = ""
    },
    TOWN{
        override fun toFieldName() = "town"
    },
    CITY{
        override fun toFieldName() = "city"
    },
    STATE{
        override fun toFieldName() = "state"
    };

    abstract fun toFieldName(): String

}