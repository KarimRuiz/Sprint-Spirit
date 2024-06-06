package com.example.sprintspirit.features.admin.data

interface Report{}

data class UserReport(
    private val type: String = "user", //message, post or user
    val reason: String = "",
    val itemId: String = ""
) : Report

data class MessageReport(
    private val type: String = "message", //message, post or user
    val reason: String = "",
    val itemId: String = "", //chat's id
    val messageId: String = ""
) : Report

data class PostReport(
    private val type: String = "message", //message, post or user
    val reason: String = "",
    val itemId: String = ""
) : Report