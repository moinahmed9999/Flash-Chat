package com.moin.flashchat.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.moin.flashchat.R
import com.moin.flashchat.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}