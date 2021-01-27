package com.moin.flashchat.data.model

data class Chat(
    val cid: String = "",
    val chatTitle: String = "",
    val chatType: Int = -1,
    val groupName: String? = ""
)