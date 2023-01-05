package com.example.loveapp.ui.account.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.loveapp.R
import com.example.loveapp.data.Resource
import com.example.loveapp.data.User
import com.example.loveapp.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
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

        //Navigation LiveData
        authViewModel.navigate.observe(viewLifecycleOwner) {
            if (it != null) {
                findNavController().navigate(R.id.action_SignUpFragment_to_testFragment)
                authViewModel.finishNavigate()
            }
        }

        //Error Livedata
        authViewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it != null) {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                authViewModel.completeErrorMessage()
            }
        }

        //Checks if password is valid
        authViewModel.passwordValidated.observe(viewLifecycleOwner) {
            if (it != null) {
                authViewModel.signup(
                    binding.edittextUsername.text.toString(),
                    binding.edittextEmail.text.toString(),
                    binding.edittextPassword.text.toString()
                )
                authViewModel.validatedPassword()
            }
        }

        //When user signs up this code will show the error, loading bar or start the navigation
        authViewModel.signupData.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
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
                        authViewModel.addNewUser()
                        authViewModel.checkTakenUser()
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
                binding.edittextConfirmPassword.text.toString()
            )
        }

        //log in text click listener
        binding.textviewLogIn.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_LogInFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}