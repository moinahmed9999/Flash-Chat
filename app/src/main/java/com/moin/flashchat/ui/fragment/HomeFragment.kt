package com.moin.flashchat.ui.fragment

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
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
import com.moin.flashchat.databinding.FragmentHomeBinding
import com.moin.flashchat.ui.viewmodel.LogInViewModel
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    companion object {
        const val RC_READ_CONTACTS = 100
        const val TAG = "HomeFragment"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    class MyContact(val displayName: String, val phoneNumber: String)

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

//        loadContacts()
    }

    private fun initUi() {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbarFragmentHome.setupWithNavController(navController, appBarConfiguration)
    }

    private fun loadContacts() {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.READ_CONTACTS)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    getContacts()
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

    private fun getContacts() {
        launchDataLoad {
            getContactsMap()
        }
    }

    private suspend fun getContactsMap() {
        val contactList: MutableList<MyContact> = emptyList<MyContact>().toMutableList()
        val contactMap: MutableMap<String, String> = emptyMap<String, String>().toMutableMap()

        val resolver: ContentResolver = requireActivity().contentResolver;
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        cursor?.let {
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val phoneNumbers = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                    if (phoneNumbers > 0) {
                        val phoneCursor = requireActivity().contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                        phoneCursor?.let {
                            if (phoneCursor.count > 0) {
                                while (phoneCursor.moveToNext()) {
                                    val phoneNumber = phoneCursor.getString(
                                        phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                                    contactList.add(MyContact(displayName, phoneNumber))
                                }
                            }
                        }
                        phoneCursor?.close()
                    }
                }
            } else {
                Log.d(TAG, "No Contacts: ")
            }
        }
        cursor?.close()

        filter(contactList, contactMap)

        val map = contactMap.toList().sortedBy { (_, value) -> value }.toMap()

        Log.d(TAG, "size: ${map.size}")

        for(entry in map) {
            Log.d(TAG, "phoneNumber: ${entry.key}, displayName: ${entry.value}")
        }
    }

    private fun filter(contactList: MutableList<MyContact>, contactMap: MutableMap<String, String>) {
        contactList.forEach { contact ->
            contact.apply {
                val number = formatNumber(phoneNumber)
                number?.let {
                    if (number.length == 13 && number.substring(0, 3) == "+91" && Patterns.PHONE.matcher(number).matches()) {
                        contactMap[number] = contactMap[number] ?: displayName
                    }
                }
            }
        }
    }

    private fun formatNumber(phoneNumber: String): String? {
        var ans = ""
        for (char in phoneNumber)
            if (char != ' ' && char != '-' && char != '(' && char != ')') ans += char

        return if (ans.length == 10) "+91$ans"
        else if (ans.length == 14) "+${ans.substring(2)}"
        else if (ans.length == 13 && ans[0] == '+') ans
        else null
    }

    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return GlobalScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    showProgressBar()
                }
                block()
            } catch (error: Throwable) {
                Log.e(TAG, "launchDataLoad: ${error.message}")
                withContext(Dispatchers.Main) {
                    showSnackbar("Error: ${error.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    hideProgressBar()
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}