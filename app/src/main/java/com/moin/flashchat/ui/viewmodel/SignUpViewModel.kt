package com.moin.flashchat.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moin.flashchat.data.repository.SignUpRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val repository: SignUpRepository = SignUpRepository()

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
    }
}