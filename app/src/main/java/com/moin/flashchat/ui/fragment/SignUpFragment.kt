package com.moin.flashchat.ui.fragment

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.moin.flashchat.R
import com.moin.flashchat.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleErrors()

        setOnClickListeners();
    }

    private fun handleErrors() {
        binding.apply {
            tilFullName.apply {
                editText?.doOnTextChanged { text, _, _, _ ->
                    if (text.toString().isEmpty()) {
                        isErrorEnabled = true
                        error = "Name should not be empty"
                    } else {
                        isErrorEnabled = false
                    }
                }
            }

            tilEmail.apply {
                editText?.doOnTextChanged { text, _, _, _ ->
                    if (text.toString().isEmpty()) {
                        isErrorEnabled = true
                        error = "Email should not be empty"
                    } else if(!Patterns.EMAIL_ADDRESS.matcher(text!!).matches()) {
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

    private fun setOnClickListeners() {
        binding.apply {

            btnLogIn.setOnClickListener {
                it.findNavController().navigate(R.id.action_signUpFragment_to_logInFragment)
            }

            btnSignUp.setOnClickListener {
                if (!isInputValid()) {
                    showSnackbar("Invalid inputs")
                } else {
                    it.findNavController().navigate(R.id.action_signUpFragment_to_phoneNoFragment)
                }
            }

        }
    }

    private fun isInputValid(): Boolean {
        binding.apply {
            return !(tilFullName.editText?.text.toString().isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(tilEmail.editText?.text.toString()).matches() ||
                tilPassword.editText?.text.toString().length < 8)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}