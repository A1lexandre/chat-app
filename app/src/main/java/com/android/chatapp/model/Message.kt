package com.android.chatapp.model

import java.sql.Timestamp

data class Message(
    val msg: String = "",
    val timestamp: Long = 0,
    val fromId: String = "",
    val toId: String = ""
)