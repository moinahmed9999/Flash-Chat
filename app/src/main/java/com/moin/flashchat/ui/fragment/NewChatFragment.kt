package com.moin.flashchat.ui.fragment

import android.Manifest
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
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.moin.flashchat.R
import com.moin.flashchat.data.adapter.ContactsAdapter
import com.moin.flashchat.data.repository.NewChatRepository
import com.moin.flashchat.databinding.FragmentNewChatBinding
import com.moin.flashchat.ui.viewmodel.NewChatViewModel

class NewChatFragment : Fragment() {

    companion object {
        const val TAG = "NewChatFragment"
    }

    private var _binding: FragmentNewChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NewChatViewModel
    private lateinit var adapter: ContactsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()

        observeViewModel()

        askPermission()
    }

    private fun initUi() {
        viewModel = ViewModelProvider(this).get(NewChatViewModel::class.java)

        adapter = ContactsAdapter()
        binding.rvContacts.adapter = adapter

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbarFragmentNewChat.setupWithNavController(navController, appBarConfiguration)

        binding.toolbarFragmentNewChat.title = getString(R.string.new_chat)
    }

    private fun observeViewModel() {
        viewModel.apply {
            spinner.observe(viewLifecycleOwner) { visible ->
                if (visible) showProgressBar() else hideProgressBar()
            }

            snackBar.observe(viewLifecycleOwner) { message ->
                message?.let {
                    showSnackbar(message)
                    onSnackbarShown()
                }
            }

            users.observe(viewLifecycleOwner) { userList ->
                adapter.submitList(userList)
            }
        }
    }

    private fun askPermission() {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.READ_CONTACTS)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    viewModel.getUsersInContact(requireActivity())
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Log.d(TAG, "onPermissionDenied: Permission Denied")
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

            }).check()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}