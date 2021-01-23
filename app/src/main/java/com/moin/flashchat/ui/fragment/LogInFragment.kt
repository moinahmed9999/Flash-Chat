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
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.moin.flashchat.R
import com.moin.flashchat.databinding.FragmentLogInBinding
import com.moin.flashchat.ui.viewmodel.LogInViewModel

class LogInFragment : Fragment() {

    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LogInViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
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
        viewModel = ViewModelProvider(this).get(LogInViewModel::class.java)
    }

    private fun handleErrors() {
        binding.apply {
            tilEmail.apply {
                editText?.doOnTextChanged { text, _, _, _ ->
                    if (text.toString().isEmpty()) {
                        isErrorEnabled = true
                        error = "Email should not be empty"
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(text!!).matches()) {
                        isErrorEnabled = true
                        error = "Invalid Email"
                    } else {
                        isErrorEnabled = false
                    }
                }
            }

            tilPassword.apply {
                editText?.doOnTextChanged { text, _, _, _ ->
                    if (text.toString().length < 8) {
                        isErrorEnabled = true
                        error = "Password should be min 8 characters long"
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
                    onSnackbarShown()
                }
            }

            signIn.observe(viewLifecycleOwner) { successful ->
                if (successful) {

                }
            }
        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            btnSignUp.setOnClickListener {
                it.findNavController().navigate(R.id.action_logInFragment_to_signUpFragment)
            }

            btnLogIn.setOnClickListener {
                if (!isInputValid()) {
                    showSnackbar("Invalid inputs")
                } else {
                    viewModel.logInUserWithEmail(
                            binding.tilEmail.editText?.text.toString(),
                            binding.tilPassword.editText?.text.toString()
                    )
                }
            }

            btnLogInWithGoogle.setOnClickListener {

            }
        }
    }

    private fun isInputValid(): Boolean {
        binding.apply {
            return (Patterns.EMAIL_ADDRESS.matcher(tilEmail.editText?.text.toString()).matches() &&
                    tilPassword.editText?.text.toString().length >= 8)
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