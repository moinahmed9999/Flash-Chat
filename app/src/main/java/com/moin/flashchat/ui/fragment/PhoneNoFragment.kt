package com.moin.flashchat.ui.fragment

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.moin.flashchat.R
import com.moin.flashchat.databinding.FragmentPhoneNoBinding
import com.moin.flashchat.ui.viewmodel.PhoneNoViewModel
import com.moin.flashchat.ui.viewmodel.SignUpViewModel

class PhoneNoFragment : Fragment() {

    private var _binding: FragmentPhoneNoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PhoneNoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhoneNoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()

        handleErrors()

        observeViewModel()

        setOnClickListeners()
    }

    private fun initUi() {
        viewModel = ViewModelProvider(this).get(PhoneNoViewModel::class.java)
    }

    private fun handleErrors() {
        binding.apply {
            tilPhoneNo.apply {
                editText?.doOnTextChanged { text, _, _, _ ->
                    if (text.toString().isEmpty()) {
                        isErrorEnabled = true
                        error = "Phone Number should not be empty"
                    } else if (text.toString().length<10){
                        isErrorEnabled = true
                        error = "Enter a 10 digit number"
                    } else {
                        isErrorEnabled = false
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.apply {
            spinner.observe(viewLifecycleOwner) { visible ->
                if (visible) showProgressBar() else hideProgressBar()
            }

            snackBar.observe(viewLifecycleOwner) { message ->
                message?.let {
                    showSnackbar(message)
                    viewModel.onSnackbarShown()
                }
            }

            signUp.observe(viewLifecycleOwner) { successful ->
//                if (successful) findNavController().navigate(R.id.action_signUpFragment_to_phoneNoFragment)
            }
        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            btnSendOtp.setOnClickListener {
                if (!isInputValid()) {
                    showSnackbar("Invalid inputs")
                } else {
                    viewModel.sendVerificationCode(
                        "+91" + tilPhoneNo.editText?.text.toString(),
                        requireActivity()
                    )
                }
            }

            btnVerifyOtp.setOnClickListener {
                viewModel.linkPhoneWithAccount(tilOtpCode.editText?.text.toString())
            }

            btnResendOtp.setOnClickListener {
                viewModel.resendVerificationCode(
                    "+91" + tilPhoneNo.editText?.text.toString(),
                    requireActivity()
                )
            }
        }
    }

    private fun isInputValid(): Boolean {
        binding.apply {
            return (tilPhoneNo.editText?.text.toString().isNotEmpty() &&
                    tilPhoneNo.editText?.text.toString().length == 10)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showProgressBar() {
        binding.llDisabledScreen.visibility = View.VISIBLE
        binding.circularProgressIndicator.show()
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressBar() {
        binding.circularProgressIndicator.hide()
        binding.llDisabledScreen.visibility = View.GONE
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}