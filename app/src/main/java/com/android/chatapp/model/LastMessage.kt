package com.android.chatapp.model

data class LastMessage(
    val photoUrl: String = "",
    val userName: String = "",
    val textMessage: String = "",
    val timestamp: Long = 0,
    val userId: String = ""
)