package com.moin.flashchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class FlashChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}