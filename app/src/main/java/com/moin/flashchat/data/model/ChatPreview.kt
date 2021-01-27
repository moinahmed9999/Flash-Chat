package com.moin.flashchat.data.model

import java.util.Date

data class ChatPreview(
    val cid: String = "",
    val chatTitle: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Date? = null
)