package com.moin.flashchat.ui.viewmodel

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.moin.flashchat.R
import com.moin.flashchat.data.repository.LogInRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LogInViewModel: ViewModel() {
    private val repository: LogInRepository = LogInRepository()

    private lateinit var googleSingInClient: GoogleSignInClient

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _snackBar = repository.snackBar
    val snackBar: LiveData<String?>
        get() = _snackBar

    val signIn = repository.signIn

    // Email
    fun logInUserWithEmail(email: String, password: String) {
        launchDataLoad {
            repository.logInUserWithEmail(email, password)
        }
    }

    // Google
    fun logInUserWithGoogle(fragment: Fragment) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(fragment.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSingInClient = GoogleSignIn.getClient(fragment.requireActivity(), gso)

        googleSingInClient.signOut()

        val intent = googleSingInClient.signInIntent
        fragment.startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        launchDataLoad {
            repository.logInUserWithGoogle(task)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    // Utility functions
    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                Log.e(TAG, "launchDataLoad: ${error.message}")
                _snackBar.value = "Error: ${error.message}"
            } finally {
                _spinner.value = false
            }
        }
    }

    fun onSnackbarShown() {
        _snackBar.value = null
    }

    companion object {
        private const val TAG = "LogInViewModel"
        private const val RC_SIGN_IN = 1
    }
}