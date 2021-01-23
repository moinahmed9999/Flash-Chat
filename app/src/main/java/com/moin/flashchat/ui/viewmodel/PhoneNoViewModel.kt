package com.moin.flashchat.ui.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moin.flashchat.data.repository.PhoneNoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PhoneNoViewModel: ViewModel() {
    private val repository: PhoneNoRepository = PhoneNoRepository()

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _snackBar = repository.snackBar
    val snackBar: LiveData<String?>
        get() = _snackBar

    val signUp = repository.signUp

    val codeSent = repository.codeSent

    // Phone
    fun sendVerificationCode(number: String, activity: Activity) {
        repository.sendVerificationCode(number, activity)
    }

    fun resendVerificationCode(number: String, activity: Activity) {
        repository.resendVerificationCode(number, activity)
    }

    fun linkPhoneWithAccount(code: String) {
        launchDataLoad {
            repository.linkPhoneWithAccount(code)
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

    fun onTimerStarted() {
        repository.onTimerStarted()
    }

    companion object {
        private const val TAG = "PhoneNoViewModel"
    }
}