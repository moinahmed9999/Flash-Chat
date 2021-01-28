package com.moin.flashchat.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.extension.await
import com.moin.flashchat.utils.Result

class LogInRepository {
    private var auth = Firebase.auth
    private val db = Firebase.firestore
    private val collection = db.collection(USER_COLLECTION_NAME)

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

    suspend fun logInUserWithGoogle(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            account?.let { account ->
                account.email?.let { email ->
                    if (doesUserExists(email)) {
                        val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                        when(val result = auth.signInWithCredential(credential).await()) {
                            is Result.Success -> {
                                Log.d(TAG, "logInUserWithGoogle: Sign In Successful ${result.data?.user?.displayName} ${result.data.user?.phoneNumber}")
                                snackBar.value = "Sign In ${result.data?.user?.displayName} ${result.data.user?.phoneNumber}"
                                _signIn.value = true
                            }
                            is Result.Error -> {
                                Log.e(TAG, "logInUserWithGoogle: ${result.exception.message}")
                                snackBar.value = result.exception.message
                            }
                            is Result.Canceled -> {
                                Log.e(TAG, "logInUserWithGoogle: ${result.exception?.message}")
                                snackBar.value = "Result Canceled"
                            }
                        }
                    } else {
                        snackBar.value = "Account with this email address does not exist"
                    }
                }
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "logInUserWithGoogle: ${e.message}")
            snackBar.value = "Sign In Failed: ${e.message}"
        }
    }

    private suspend fun doesUserExists(email: String): Boolean {
        when (val result = collection.whereEqualTo("email", email).get().await()) {
            is Result.Success -> {
                val size = result.data.documents.size
                if (size > 0) {
                    Log.d(TAG, "doesUserExists: true")
                    return true
                }
            }
            is Result.Error -> {
                Log.e(TAG, "doesUserExists: ${result.exception.message}")
                snackBar.value = result.exception.message
            }
            is Result.Canceled -> {
                Log.e(TAG, "doesUserExists: ${result.exception?.message}")
                snackBar.value = "Result Canceled"
            }
        }
        Log.d(TAG, "doesUserExists: false")
        return false
    }

    companion object {
        private const val TAG = "LogInRepository"
        private const val USER_COLLECTION_NAME = "users"
    }
}