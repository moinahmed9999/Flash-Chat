package com.moin.flashchat.data.model

import com.google.firebase.Timestamp

data class ChatPreview(
    val cid: String = "",
    val chatTitle: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp? = null
)