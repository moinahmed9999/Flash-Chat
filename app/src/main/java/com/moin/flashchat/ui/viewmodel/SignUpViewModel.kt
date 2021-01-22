package com.moin.flashchat.ui.viewmodel

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.moin.flashchat.R
import com.moin.flashchat.data.repository.SignUpRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val repository: SignUpRepository = SignUpRepository()

    private lateinit var googleSingInClient: GoogleSignInClient

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _snackBar: MutableLiveData<String?> = repository.snackBar
    val snackBar: LiveData<String?>
        get() = _snackBar

    fun signUpUserWithEmail(name: String, email: String, password: String) {
        launchDataLoad {
            repository.signUpUserWithEmail(name, email, password)
        }
    }

    fun signUpUserWithGoogle(fragment: Fragment) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(fragment.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSingInClient = GoogleSignIn.getClient(fragment.requireActivity(), gso)

        val intent = googleSingInClient.signInIntent
        fragment.startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun handleGoogleSignInResult (task: Task<GoogleSignInAccount>) {
        launchDataLoad {
            repository.signUpUserWithGoogle(task)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

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
        private const val TAG = "SignUpViewModel"
        private const val RC_SIGN_IN = 1
    }
}