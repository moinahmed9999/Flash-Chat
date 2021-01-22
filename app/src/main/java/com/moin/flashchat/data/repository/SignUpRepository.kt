package com.moin.flashchat.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.extension.await
import com.moin.flashchat.utils.Result

class SignUpRepository {
    private var auth = Firebase.auth
//    private val db = Firebase.firestore
//    private val collection = db.collection(USER_COLLECTION_NAME)

    val snackBar = MutableLiveData<String?>()

    suspend fun signUpUserWithEmail(name: String, email: String, password: String) {
        try {
            when (val resultAuthResult = auth.createUserWithEmailAndPassword(email, password).await()) {
                is Result.Success -> {
                    Log.i(TAG, "signUpUserWithEmail: Result.Success")
                    val user = resultAuthResult.data.user
                    updateUserProfile(name, user)
                }
                is Result.Error -> {
                    Log.e(TAG, "signUpUserWithEmail1: ${resultAuthResult.exception}")
                    snackBar.value = resultAuthResult.exception.message
                }
                is Result.Canceled -> {
                    Log.e(TAG, "signUpUserWithEmail2: ${resultAuthResult.exception}")
                    snackBar.value = "Request Cancelled"
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "signUpUserWithEmail3: ${error.message}")
            snackBar.value = "Error: ${error.message}"
        }
    }

    private suspend fun updateUserProfile(name: String, user: FirebaseUser?) {
        try {
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }

            when (val result = user?.updateProfile(profileUpdates)?.await()) {
                is Result.Success -> {
                    Log.i(TAG, "updateUserProfile: Sign Up Successful ${auth.currentUser?.displayName}")
                    snackBar.value = "Sign Up Successful ${user.displayName}"
                }
                is Result.Error -> {
                    Log.e(TAG, "updateUserProfile1: ${result.exception}")
                    snackBar.value = result.exception.message
                }
                is Result.Canceled -> {
                    Log.e(TAG, "updateUserProfile2: ${result.exception}")
                    snackBar.value = "Request Cancelled"
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "updateUserProfile3: ${error.message}")
            snackBar.value = "Error: ${error.message}"
        }
    }

    companion object {
        private const val TAG = "SignUpRepository"
//        private const val USER_COLLECTION_NAME = "users"
    }
}