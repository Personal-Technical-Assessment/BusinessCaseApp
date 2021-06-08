package com.gozem.test.businesscase.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gozem.test.businesscase.R
import kotlinx.android.synthetic.main.fragment_splash_screen.*

class SplashScreenFragment : Fragment() {

    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_splash_screen,
            container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
    }

    private fun initListener() {
        signUpButton.setOnClickListener {
            findNavController()
                .navigate(R.id.action_splashScreenFragment_to_signUpFragment)
        }

        signInButton.setOnClickListener {
            findNavController()
                .navigate(R.id.action_splashScreenFragment_to_signInFragment)
        }
    }
}