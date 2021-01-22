package com.moin.flashchat.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.extension.await
import com.moin.flashchat.utils.Result

class SignUpRepository() {
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
        } catch (e: Exception) {
            Log.e(TAG, "signUpUserWithEmail3: ${e.message}")
            snackBar.value = "Exception: ${e.message}"
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
                    Log.e(TAG, "updateUserProfile1: ${result.exception.message}")
                    snackBar.value = result.exception.message
                }
                is Result.Canceled -> {
                    Log.e(TAG, "updateUserProfile2: ${result.exception?.message}")
                    snackBar.value = "Request Cancelled"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateUserProfile3: ${e.message}")
            snackBar.value = "Exception: ${e.message}"
        }
    }

    suspend fun signUpUserWithGoogle(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            account?.let {
                val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                when(val result = auth.signInWithCredential(credential).await()) {
                    is Result.Success -> {
                        Log.d(TAG, "signUpUserWithGoogle: Sign Up Successful ${result.data?.user?.displayName}")
                        snackBar.value = "Sign Up Successful ${result.data?.user?.displayName}"
                    }
                    is Result.Error -> {
                        Log.e(TAG, "signUpUserWithGoogle: ${result.exception.message}")
                        snackBar.value = result.exception.message
                    }
                    is Result.Canceled -> {
                        Log.e(TAG, "signUpUserWithGoogle: ${result.exception?.message}")
                        snackBar.value = "Result Canceled"
                    }
                }
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "signUpUserWithGoogle: ${e.message}")
            snackBar.value = "Sign In Failed: ${e.message}"
        }
    }

    companion object {
        private const val TAG = "SignUpRepository"
//        private const val USER_COLLECTION_NAME = "users"
    }
}