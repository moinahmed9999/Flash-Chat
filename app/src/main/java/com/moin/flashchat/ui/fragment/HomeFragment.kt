package com.moin.flashchat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.R
import com.moin.flashchat.data.adapter.ChatPreviewAdapter
import com.moin.flashchat.data.model.ChatPreview
import com.moin.flashchat.data.repository.NewChatRepository
import com.moin.flashchat.databinding.FragmentHomeBinding
import com.moin.flashchat.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
        private const val USER_COLLECTION_NAME = "users"
        private const val CHAT_COLLECTION_NAME = "chats"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: ChatPreviewAdapter

    private var auth = Firebase.auth
    private val db = Firebase.firestore
    private val usersCollection = db.collection(USER_COLLECTION_NAME)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()

        setOnClickListeners()
    }

    private fun initUi() {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val uid = auth.currentUser?.uid!!

        val query = usersCollection.document(uid)
            .collection(CHAT_COLLECTION_NAME)
            .orderBy("chatTitle")

        val options = FirestoreRecyclerOptions.Builder<ChatPreview>()
            .setQuery(query, ChatPreview::class.java)
            .build()

        adapter = ChatPreviewAdapter(options, object : ChatPreviewAdapter.ChatClickListener {
            override fun onChatClick(position: Int, chatPreview: ChatPreview) {
                findNavController().navigate(R.id.action_homeFragment_to_chatFragment)
            }
        })

        binding.rvChats.adapter = adapter

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbarFragmentHome.setupWithNavController(navController, appBarConfiguration)

        binding.toolbarFragmentHome.title = getString(R.string.app_name)
    }

    private fun setOnClickListeners() {
        binding.apply {
            fabNewChat.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_newChatFragment)
            }
        }
    }

    private fun showSnackbar(message: String) {
        Log.d(TAG, "showSnackbar:")
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showProgressBar() {
        Log.d(TAG, "showProgressBar:")
        binding.llDisabledScreen.visibility = View.VISIBLE
        binding.circularProgressIndicator.show()
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressBar() {
        Log.d(TAG, "hideProgressBar:")
        binding.circularProgressIndicator.hide()
        binding.llDisabledScreen.visibility = View.GONE
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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