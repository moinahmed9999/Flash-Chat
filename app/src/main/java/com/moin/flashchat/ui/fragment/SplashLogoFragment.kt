package com.moin.flashchat.ui.fragment

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.moin.flashchat.R
import com.moin.flashchat.databinding.FragmentSplashLogoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashLogoFragment : Fragment() {

    private var _binding: FragmentSplashLogoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashLogoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragmentSplashMotionLayout.setTransitionDuration(MOTION_TRANSITION_DURATION)

//        GlobalScope.launch(Dispatchers.Main) {
//            delay(MOTION_TRANSITION_DURATION.toLong())
//            Navigation.findNavController(binding.root)
//                .navigate(R.id.action_splashLogoFragment_to_signInActivity)
//            activity?.finish()
//        }

        Handler().postDelayed(Runnable {
            Navigation.findNavController(view).navigate(R.id.action_splashLogoFragment_to_signInActivity)
//            findNavController().navigate(R.id.action_splashLogoFragment_to_signInActivity)
            activity?.finish()
        }, MOTION_TRANSITION_DURATION.toLong())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val MOTION_TRANSITION_DURATION = 2000
    }
}