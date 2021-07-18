package com.moin.flashchat.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.moin.flashchat.R
import com.moin.flashchat.data.adapter.MessageAdapter
import com.moin.flashchat.data.model.Chat
import com.moin.flashchat.data.model.ChatPreview
import com.moin.flashchat.data.model.Message
import com.moin.flashchat.databinding.FragmentChatBinding
import com.moin.flashchat.ui.viewmodel.ChatViewModel
import com.moin.flashchat.ui.viewmodel.ViewModelFactory

class ChatFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
        private const val CHAT_COLLECTION_NAME = "chats"
        private const val MESSAGE_COLLECTION_NAME = "messages"
    }

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessageAdapter

    private var auth = Firebase.auth
    private val db = Firebase.firestore
    private val chatsCollection = db.collection(CHAT_COLLECTION_NAME)

    private var type = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()

        setOnClickListeners()
    }

    private fun initUi() {
        val chat = gson.fromJson(arguments?.getString("chat"), Chat::class.java)
        type = chat.chatType

        viewModel = ViewModelProvider(this, ViewModelFactory(chat.cid)).get(ChatViewModel::class.java)

        val query = chatsCollection.document(chat.cid)
                .collection(MESSAGE_COLLECTION_NAME)
                .orderBy("timestamp", Query.Direction.ASCENDING)

        val options = FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message::class.java)
                .build()

        adapter = MessageAdapter(options)

        binding.apply {
            rvChats.adapter = adapter
            val layoutManager = LinearLayoutManager(this@ChatFragment.context)
            layoutManager.stackFromEnd = true
            rvChats.layoutManager = layoutManager
        }

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbarFragmentChat.setupWithNavController(navController, appBarConfiguration)

        binding.toolbarFragmentChat.title = chat?.chatTitle
    }

    private fun setOnClickListeners() {
        binding.apply {
            btnSend.setOnClickListener {
                val message = etMessage.text?.toString()
                if (!message.isNullOrEmpty()) {
                    if (type == 1) viewModel.sendMessage(message)
                    else if (type == 2) viewModel.sendGroupMessage(message)
                    etMessage.setText("")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}