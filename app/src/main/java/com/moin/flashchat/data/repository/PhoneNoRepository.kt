package com.moin.flashchat.data.repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.data.model.BasicUser
import com.moin.flashchat.data.model.User
import com.moin.flashchat.extension.await
import com.moin.flashchat.utils.Result
import java.util.concurrent.TimeUnit

class PhoneNoRepository {
    private var auth = Firebase.auth
    private val db = Firebase.firestore
    private val collection = db.collection(USER_COLLECTION_NAME)
    private lateinit var user: FirebaseUser

    val snackBar = MutableLiveData<String?>()

    private val _signUp = MutableLiveData(false)
    val signUp: LiveData<Boolean>
        get() = _signUp

    private val _codeSent = MutableLiveData(false)
    val codeSent: LiveData<Boolean>
        get() = _codeSent

    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    init {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: $credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    snackBar.value = "Invalid Credentials"
                } else if (e is FirebaseTooManyRequestsException) {
                    snackBar.value = "SMS quota exceeded"
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent: $verificationId")
                snackBar.value = "Code sent"
                _codeSent.value = true

                storedVerificationId = verificationId
                resendToken = token
            }
        }
    }

    fun sendVerificationCode(
        number: String,
        activity: Activity
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendVerificationCode(
        number: String,
        activity: Activity
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)    // ForceResendingToken from callbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun linkPhoneWithAccount(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        try {
            when (val result = auth.currentUser!!.linkWithCredential(credential).await()) {
                is Result.Success -> {
                    Log.d(TAG, "linkPhoneWithAccount: Sign Up Successful ${result.data?.user?.phoneNumber}")
                    user = result.data.user!!
                    snackBar.value = "Sign Up Successful ${result.data?.user?.phoneNumber}"
                    createUserInFirestore()
                }
                is Result.Error -> {
                    Log.e(TAG, "linkPhoneWithAccount: ${result.exception.message}")
                    snackBar.value = result.exception.message
                }
                is Result.Canceled -> {
                    Log.e(TAG, "linkPhoneWithAccount: ${result.exception?.message}")
                    snackBar.value = "Result Canceled"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "linkPhoneWithAccount: ${e.message}")
            snackBar.value = "Verification Failed: ${e.message}"
        }
    }

    private suspend fun createUserInFirestore() {
        try {
            user.apply {

                val uidRef = collection.document(uid)
                val phoneNumberRef = collection.document(phoneNumber!!)

                val result = db.runTransaction { transaction ->
                    transaction.set(phoneNumberRef, BasicUser(
                        uid, displayName!!, email!!, phoneNumber!!))

                    transaction.set(uidRef, User(
                        uid, displayName!!, email!!, phoneNumber!!, emptyList(), emptyList()))
                }.await()

                when (result) {
                    is Result.Success -> {
                        Log.d(TAG, "createUserInFirestore: Added in firestore")
                        snackBar.value = "Added in firestore"
                        _signUp.value = true
                    }
                    is Result.Error -> {
                        Log.e(TAG, "createUserInFirestore: ${result.exception.message}")
                        snackBar.value = result.exception.message
                    }
                    is Result.Canceled -> {
                        Log.e(TAG, "createUserInFirestore: ${result.exception?.message}")
                        snackBar.value = "Result Canceled"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "createUserInFirestore: ${e.message}")
            snackBar.value = "Could not add user to firestore: ${e.message}"
        }
    }

    fun onTimerStarted() {
        _codeSent.value = false
    }

    companion object {
        private const val TAG = "PhoneNoRepository"
        private const val USER_COLLECTION_NAME = "users"
    }
}