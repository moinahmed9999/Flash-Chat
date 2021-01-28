package com.moin.flashchat.data.model

import java.util.Date

data class ChatPreview(
    val cid: String = "",
    val chatTitle: String = "",
    val chatType: Int = -1,
    val lastMessage: String = "",
    val lastMessageTimestamp: Date? = null
)