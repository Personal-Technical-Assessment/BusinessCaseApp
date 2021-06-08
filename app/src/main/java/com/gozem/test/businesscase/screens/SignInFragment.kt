package com.gozem.test.businesscase.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gozem.test.businesscase.R
import com.gozem.test.businesscase.application.appContext
import com.gozem.test.businesscase.utils.Utils.changeColorOfPartOfString
import com.gozem.test.businesscase.utils.Utils.displayToastMessage
import com.gozem.test.businesscase.utils.Utils.md5Hash
import com.gozem.test.businesscase.utils.Utils.validateUserSignInCredentials
import com.gozem.test.businesscase.viewModels.MainViewModel
import kotlinx.android.synthetic.main.fragment_sign_in.*

class SignInFragment : Fragment() {

    private lateinit var root: View
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_sign_in,
            container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListener()
        initObservable()
    }

    private fun initView() {
        val text = getString(R.string.new_here_sign_in_string)
        signUpText.text = changeColorOfPartOfString(
            requireContext(),
            text,
            R.color.light_green,
            0,
            text.length,
            false
        )
    }

    private fun initListener() {
        imgBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_splashScreenFragment)
        }
        signInButton.setOnClickListener {
            val error = validateUserSignInCredentials(
                appContext,
                editEmail.text.toString().trim(),
                editPassword.text.toString().trim()
            )
            if (error.isNotEmpty()) {
                displayToastMessage(error)
            } else {
                mainViewModel.checkUserCredentials(
                    requireActivity(),
                    editEmail.text.toString().trim(),
                    md5Hash(editPassword.text.toString().trim())
                )
            }
        }

        signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
    }

    private fun initObservable() {
        mainViewModel.errorMessage.observe(viewLifecycleOwner, {
            displayToastMessage(it)
        })
    }
}