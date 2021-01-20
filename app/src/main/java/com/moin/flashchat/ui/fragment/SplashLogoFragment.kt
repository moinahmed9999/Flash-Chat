package com.moin.flashchat.ui.fragment

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.moin.flashchat.R
import com.moin.flashchat.databinding.FragmentSplashLogoBinding

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

        Handler().postDelayed(Runnable {
            Navigation.findNavController(view)
                .navigate(R.id.action_splashLogoFragment_to_signInActivity)
            activity?.finish()
        }, MOTION_TRANSITION_DURATION.toLong())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MOTION_TRANSITION_DURATION = 2000
    }
}