package com.moin.flashchat.ui.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moin.flashchat.data.model.BasicUser
import com.moin.flashchat.data.repository.NewChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NewChatViewModel: ViewModel() {
    private val repository: NewChatRepository = NewChatRepository()

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _snackBar = repository.snackBar
    val snackBar: LiveData<String?>
        get() = _snackBar

    val users = repository.users

    val chat = repository.chat

    fun getUsersInContact(activity: Activity) {
        launchDataLoad {
            repository.getUsersInContact(activity)
        }
    }

    fun startChat(contact: BasicUser) {
        launchDataLoad {
            repository.startChat(contact)
        }
    }

    fun createNewGroup(groupName: String, groupIds: List<String>) {
        launchDataLoad {
            repository.createNewGroup(groupName, groupIds)
        }
    }

    // Utility functions
    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            try {
                _spinner.postValue(true)
                block()
            } catch (error: Throwable) {
                Log.e(TAG, "launchDataLoad: ${error.message}")
                _snackBar.postValue("Error: ${error.message}")
            } finally {
                _spinner.postValue(false)
            }
        }
    }

    fun chatStarted() {
        repository.chatStarted()
    }

    fun onSnackbarShown() {
        _snackBar.value = null
    }

    companion object {
        private const val TAG = "NewChatViewModel"
    }
}