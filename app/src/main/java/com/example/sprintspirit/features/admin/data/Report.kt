package com.example.sprintspirit.features.admin.data

interface Report{}

data class UserReport(
    val type: String = "user", //message, post or user
    val reporter: String = "",
    val reason: String = "",
    val itemId: String = ""
) : Report

data class MessageReport(
    val type: String = "message", //message, post or user
    val reporter: String = "",
    val reason: String = "",
    val itemId: String = "", //chat's id
    val messageId: String = ""
) : Report

data class PostReport(
    val type: String = "message", //message, post or user
    val reporter: String = "",
    val reason: String = "",
    val itemId: String = ""
) : Report