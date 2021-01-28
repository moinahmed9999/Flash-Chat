package com.moin.flashchat.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moin.flashchat.data.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatViewModel(private val cid: String) : ViewModel() {
    private val repository: ChatRepository = ChatRepository(cid)

    fun sendMessage(message: String) {
        launchDataLoad {
            repository.sendMessage(message)
        }
    }

    fun sendGroupMessage(message: String) {
        launchDataLoad {
            repository.sendGroupMessage(message)
        }
    }

    // Utility functions
    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            try {
//                _spinner.postValue(true)
                block()
            } catch (error: Throwable) {
                Log.e(TAG, "launchDataLoad: ${error.message}")
//                _snackBar.postValue("Error: ${error.message}")
            } finally {
//                _spinner.postValue(false)
            }
        }
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}