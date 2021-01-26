package com.moin.flashchat.data.repository

import android.app.Activity
import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.data.model.BasicUser
import com.moin.flashchat.data.model.Chat
import com.moin.flashchat.data.model.ChatPreview
import com.moin.flashchat.data.model.Contact
import com.moin.flashchat.extension.await
import com.moin.flashchat.ui.fragment.HomeFragment
import com.moin.flashchat.utils.Result

class NewChatRepository {
    private var auth = Firebase.auth
    private val db = Firebase.firestore
    private val usersCollection = db.collection(USER_COLLECTION_NAME)
    private val chatsCollection = db.collection(CHAT_COLLECTION_NAME)
    private val currentUser = auth.currentUser!!

    val snackBar = MutableLiveData<String?>()

    private val _chat = MutableLiveData<Chat?>()
    val chat: LiveData<Chat?>
        get() = _chat

    private var contactList: MutableList<Contact> = mutableListOf()
    private lateinit var contactMap: Map<String, String>
    private var userList: MutableList<BasicUser> = mutableListOf()

    private val _users: MutableLiveData<List<BasicUser>> = MutableLiveData<List<BasicUser>>()
    val users: LiveData<List<BasicUser>>
        get() = _users

    suspend fun getUsersInContact(activity: Activity) {
        getContactList(activity)

        try {
            val result = usersCollection.get().await()
            val currentUid = currentUser.uid

            if (result is Result.Success) {
                result.data.documents.forEach { documentSnapshot ->
                    val documentId = documentSnapshot.id
                    if (documentId.length == 13 && contactMap[documentId] != null) {
                        documentSnapshot.toObject<BasicUser>()?.let {
                            if (it.uid != currentUid) userList.add(it)
                        }
                    }
                }
            }

            val contactIds: List<String> = userList.map { user -> user.uid }.filter { it !=  currentUid}

            usersCollection.document(currentUid).update("contactIds", contactIds).await()

            _users.postValue(userList)

        } catch (e: Exception) {
            Log.e(TAG, "getUsersInContact: ${e.message}")
            snackBar.postValue("Verification Failed: ${e.message}")
        }
    }

    private suspend fun isUser(phoneNumber: String): Pair<Boolean, BasicUser?> {
        val result = usersCollection.document(phoneNumber).get().await()
        if (result is Result.Success && result.data.exists()) {
            return Pair(true, result.data.toObject<BasicUser>())
        }
        return Pair(false, null)
    }

    private fun getContactList(activity: Activity) {
        contactList.clear()

        val contactMutableMap: MutableMap<String, String> = mutableMapOf()

        val resolver: ContentResolver = activity.contentResolver;
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        cursor?.let {
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val phoneNumbers = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                    if (phoneNumbers > 0) {
                        val phoneCursor = activity.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                        phoneCursor?.let {
                            if (phoneCursor.count > 0) {
                                while (phoneCursor.moveToNext()) {
                                    val phoneNumber = phoneCursor.getString(
                                        phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                                    contactList.add(Contact(phoneNumber, displayName))
                                }
                            }
                        }
                        phoneCursor?.close()
                    }
                }
            } else {
                Log.d(HomeFragment.TAG, "No Contacts: ")
            }
        }
        cursor?.close()

        filter(contactList, contactMutableMap)

        contactMap = contactMutableMap.toList().sortedBy { (_, value) -> value }.toMap()

        contactList = contactMutableMap.toList().sortedBy { (_, value) -> value }.toMutableList().map { pair ->
            Contact(pair.first, pair.second)
        }.toMutableList()
    }

    private fun filter(contactList: MutableList<Contact>, contactMap: MutableMap<String, String>) {
        contactList.forEach { contact ->
            contact.apply {
                val number = formatNumber(phoneNumber)
                number?.let {
                    if (number.length == 13 && number.substring(0, 3) == "+91" && Patterns.PHONE.matcher(number).matches()) {
                        contactMap[number] = contactMap[number] ?: displayName
                    }
                }
            }
        }
    }

    private fun formatNumber(phoneNumber: String): String? {
        var ans = ""
        for (char in phoneNumber)
            if (char != ' ' && char != '-' && char != '(' && char != ')') ans += char

        return if (ans.length == 10) "+91$ans"
        else if (ans.length == 14) "+${ans.substring(2)}"
        else if (ans.length == 13 && ans[0] == '+') ans
        else null
    }

    suspend fun startChat(contact: BasicUser) {
        try {
            val cid = getChatId(contact.uid, currentUser.uid)
            val result = chatsCollection.document(cid).get().await()
            if (result is Result.Success && result.data.exists()) {
                val userResult = usersCollection.document(currentUser.phoneNumber!!).get().await()
                if (userResult is Result.Success && userResult.data.exists()) {
                    Log.e(TAG, "Chat already exists")
                    snackBar.postValue("Chat already exists")
                    _chat.postValue(Chat(cid, userResult.data.toObject<BasicUser>()!!, contact, 1, null))
                }
            } else {
                val transactionResult = db.runTransaction { transaction ->
                    val u1Ref = usersCollection.document(currentUser.uid).collection(
                        CHAT_COLLECTION_NAME).document(cid)

                    val u2Ref = usersCollection.document(contact.uid).collection(
                        CHAT_COLLECTION_NAME).document(cid)

                    val groupRef = chatsCollection.document(cid)

                    transaction.set(u1Ref, ChatPreview(cid, contact.displayName, "", null))

                    transaction.set(u2Ref, ChatPreview(cid, currentUser.displayName!!, "", null))

                    transaction.set(groupRef, mapOf(
                        "cid" to cid,
                        "type" to 1,
                        "user1" to currentUser.uid,
                        "user2" to contact.uid
                    ))
                }.await()

                if (transactionResult is Result.Success) {
                    currentUser.apply {
                        val sender = BasicUser(uid, displayName!!, email!!, phoneNumber!!)
                        Log.e(TAG, "New Chat")
                        snackBar.postValue("New Chat")
                        _chat.postValue(Chat(cid, sender, contact, 1, null))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "startChat: ${e.message}")
            snackBar.postValue("Verification Failed: ${e.message}")
        }
    }

    private fun getChatId(u1: String, u2: String): String {
        return if (u1 <= u2) "${u1}_${u2}"
        else "${u2}_${u1}"
    }

    fun chatStarted() {
        _chat.postValue(null)
    }

    companion object {
        private const val TAG = "NewChatRepository"
        private const val USER_COLLECTION_NAME = "users"
        private const val CHAT_COLLECTION_NAME = "chats"
        private const val MESSAGE_COLLECTION_NAME = "messages"
    }
}