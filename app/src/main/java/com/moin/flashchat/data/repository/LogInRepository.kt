package com.moin.flashchat.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.extension.await
import com.moin.flashchat.utils.Result

class LogInRepository {
    private var auth = Firebase.auth
//    private val db = Firebase.firestore
//    private val collection = db.collection(USER_COLLECTION_NAME)

    val snackBar = MutableLiveData<String?>()

    private val _signIn = MutableLiveData(false)
    val signIn: LiveData<Boolean>
        get() = _signIn

    suspend fun logInUserWithEmail(email: String, password: String) {
        try {
            when (val result = auth.signInWithEmailAndPassword(email, password).await()) {
                is Result.Success -> {
                    Log.i(TAG, "logInUserWith: Sign In Successful ${auth.currentUser?.displayName} ${result.data.user?.phoneNumber}")
                    snackBar.value = "Sign In ${result.data.user?.displayName} ${result.data.user?.phoneNumber}"
                    _signIn.value = true
                }
                is Result.Error -> {
                    Log.e(TAG, "logInUserWith: ${result.exception}")
                    snackBar.value = result.exception.message
                }
                is Result.Canceled -> {
                    Log.e(TAG, "logInUserWith: ${result.exception}")
                    snackBar.value = "Request Cancelled"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "logInUserWith: ${e.message}")
            snackBar.value = "Exception: ${e.message}"
        }
    }

    companion object {
        private const val TAG = "LogInRepository"
//        private const val USER_COLLECTION_NAME = "users"
    }
}