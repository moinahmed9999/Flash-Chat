package com.moin.flashchat.data.repository

import android.app.Activity
import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.data.model.BasicUser
import com.moin.flashchat.data.model.Contact
import com.moin.flashchat.extension.await
import com.moin.flashchat.ui.fragment.HomeFragment
import com.moin.flashchat.utils.Result

class NewChatRepository {
    private var auth = Firebase.auth
    private val db = Firebase.firestore
    private val collection = db.collection(USER_COLLECTION_NAME)
    private lateinit var user: FirebaseUser

    val snackBar = MutableLiveData<String?>()

    private var contactList: MutableList<Contact> = mutableListOf()
    private lateinit var contactMap: Map<String, String>
    private var userList: MutableList<BasicUser> = mutableListOf()

    private val _users: MutableLiveData<List<BasicUser>> = MutableLiveData<List<BasicUser>>()
    val users: LiveData<List<BasicUser>>
        get() = _users

    suspend fun getUsersInContact(activity: Activity) {
        getContactList(activity)

        try {
            val result = collection.get().await()
            val currentUid = auth.currentUser?.uid!!

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

            collection.document(currentUid).update("contactIds", contactIds).await()

            _users.value = userList

        } catch (e: Exception) {
            Log.e(TAG, "linkPhoneWithAccount: ${e.message}")
            snackBar.value = "Verification Failed: ${e.message}"
        }
    }

    private suspend fun isUser(phoneNumber: String): Pair<Boolean, BasicUser?> {
        val result = collection.document(phoneNumber).get().await()
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

    companion object {
        private const val TAG = "NewChatRepository"
        private const val USER_COLLECTION_NAME = "users"
    }
}