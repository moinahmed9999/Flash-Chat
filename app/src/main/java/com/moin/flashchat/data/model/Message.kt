package com.moin.flashchat.data.model

import java.util.Date

data class Message(
        val mid: String = "",
        val senderId: String = "",
        val senderName: String = "",
        val message: String = "",
        val timestamp: Date? = null
)