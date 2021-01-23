package com.moin.flashchat.ui.fragment

import android.os.Bundle
import android.os.CountDownTimer
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
    private lateinit var timer: CountDownTimer

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

        binding.apply {
            btnVerifyOtp.isEnabled = false
            btnResendOtp.isEnabled = false
        }
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
                    onSnackbarShown()
                }
            }

            signUp.observe(viewLifecycleOwner) { successful ->
                if (successful) {
                    timer.cancel()

                    binding.apply {
                        tilTimerSeconds.editText?.setText(R.string.time_initial_value)

                        btnVerifyOtp.isEnabled = false
                        btnChangePhoneNo.visibility = View.GONE
                    }

//                    findNavController().navigate(R.id.action_signUpFragment_to_phoneNoFragment)
                }
            }

            codeSent.observe(viewLifecycleOwner) { codeSent ->
                if (codeSent) {
                    binding.apply {
                        btnVerifyOtp.isEnabled = true
                        btnSendOtp.isEnabled = false
                        tilPhoneNo.editText?.isEnabled = false
                        btnChangePhoneNo.visibility = View.VISIBLE
                    }

                    showTimer()
                    onTimerStarted()
                }
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
                if (isOtpValid()) viewModel.linkPhoneWithAccount(tilOtpCode.editText?.text.toString())
            }

            btnResendOtp.setOnClickListener {
                viewModel.resendVerificationCode(
                    "+91" + tilPhoneNo.editText?.text.toString(),
                    requireActivity()
                )
            }

            btnChangePhoneNo.setOnClickListener {
                tilPhoneNo.editText?.isEnabled = true
                btnSendOtp.isEnabled = true

                btnVerifyOtp.isEnabled = false
                btnResendOtp.isEnabled = false
                timer?.cancel()
                tilTimerSeconds.editText?.setText(R.string.time_initial_value)

                btnChangePhoneNo.visibility = View.GONE
            }
        }
    }

    private fun isInputValid(): Boolean {
        binding.apply {
            return (tilPhoneNo.editText?.text.toString().isNotEmpty() &&
                    tilPhoneNo.editText?.text.toString().length == 10)
        }
    }

    private fun isOtpValid(): Boolean {
        binding.apply {
            return (tilOtpCode.editText?.text.toString().isNotEmpty() &&
                    tilOtpCode.editText?.text.toString().length == 6)
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

    private fun showTimer() {
        binding.apply {
            if (btnResendOtp.isEnabled) btnResendOtp.isEnabled = false

            timer = object : CountDownTimer(60_000, 1_000) {
                override fun onTick(millisUntilFinished: Long) {
                    val timeLeft = (millisUntilFinished/1000).toString()

                    if (timeLeft.length == 1) {
                        tilTimerSeconds.editText?.setText("0$timeLeft")
                    } else {
                        tilTimerSeconds.editText?.setText(timeLeft)
                    }
                }

                override fun onFinish() {
                    btnResendOtp.isEnabled = true
                }
            }
            timer.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}