package com.moin.flashchat.data.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val chatIds: List<String>? = null,
    val contactIds: List<String>? = null
)
