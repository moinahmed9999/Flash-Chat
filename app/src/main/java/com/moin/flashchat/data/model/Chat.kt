package com.moin.flashchat.data.model

class Chat(
    val cid: String,
    val sender: BasicUser,
    val receiver: BasicUser?,
    val cType: Int,
    val groupName: String?
)