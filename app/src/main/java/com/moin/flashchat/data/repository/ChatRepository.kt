package com.moin.flashchat.data.repository

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.data.model.Message
import com.moin.flashchat.extension.await
import com.moin.flashchat.utils.Result
import java.lang.Exception
import java.sql.Timestamp
import java.util.*

class ChatRepository(private val cid: String) {
    private var auth = Firebase.auth
    private val db = Firebase.firestore
    private val usersCollection = db.collection(USER_COLLECTION_NAME)
    private val chatsCollection = db.collection(CHAT_COLLECTION_NAME)
    private val currentUser = auth.currentUser!!

    suspend fun sendMessage(message: String) {
        try {
            val result = db.runTransaction { transaction ->
                val chatRef = chatsCollection.document(cid)
                val chatDetails = transaction.get(chatRef)

                val date = Date()

                chatDetails.getString("user1")?.let {
                    val ref = usersCollection.document(it).collection(CHAT_COLLECTION_NAME).document(cid)
                    transaction.update(ref, "lastMessage", message)
                    transaction.update(ref, "lastMessageTimestamp", date)
                }

                chatDetails.getString("user2")?.let {
                    val ref = usersCollection.document(it).collection(CHAT_COLLECTION_NAME).document(cid)
                    transaction.update(ref, "lastMessage", message)
                    transaction.update(ref, "lastMessageTimestamp", date)
                }

                val messageRef = chatRef.collection(MESSAGE_COLLECTION_NAME).document()

                transaction.set(messageRef, Message(
                        messageRef.id, currentUser.uid, currentUser.displayName!!, message, date))
            }.await()

            if (result is Result.Success) {
                Log.d(TAG, "sendMessage: Successful")
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ChatRepository"
        private const val USER_COLLECTION_NAME = "users"
        private const val CHAT_COLLECTION_NAME = "chats"
        private const val MESSAGE_COLLECTION_NAME = "messages"
    }
}