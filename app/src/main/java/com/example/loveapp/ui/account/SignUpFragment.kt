package com.example.loveapp.ui.account

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.loveapp.R
import com.example.loveapp.data.Resource
import com.example.loveapp.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel
    private var _binding: FragmentSignUpBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authViewModel =
            ViewModelProvider(this)[AuthViewModel::class.java]

        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        authViewModel.navigate.observe(viewLifecycleOwner){
            if(it != null) {
                findNavController().navigate(R.id.action_SignUpFragment_to_testFragment)
                authViewModel.finishNavigate()
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner){
            if(it != null) {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                authViewModel.completeErrorMessage()
            }
        }

        authViewModel.passwordValidated.observe(viewLifecycleOwner){
            if(it != null){
                authViewModel.signup(
                    binding.edittextUsername.text.toString(),
                    binding.edittextEmail.text.toString(),
                    binding.edittextPassword.text.toString()
                )
                authViewModel.validatedPassword()
            }
        }

        authViewModel.signupData.observe(viewLifecycleOwner){
            it?.let {
                when(it){
                    is Resource.Failure -> {
                        authViewModel.showErrorMessage(it.exception)
                        binding.buttonSignUp.visibility = View.VISIBLE
                        binding.progressbarLoading.visibility = View.GONE

                    }
                    Resource.Loading -> {
                        binding.buttonSignUp.visibility = View.INVISIBLE
                        binding.progressbarLoading.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        authViewModel.startNavigate()
                    }
                }
            }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignUp.setOnClickListener {
            authViewModel.checkPassword(
                binding.edittextPassword.text.toString(),
                binding.edittextConfirmPassword.text.toString())
        }
        binding.textviewLogIn.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_LogInFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}