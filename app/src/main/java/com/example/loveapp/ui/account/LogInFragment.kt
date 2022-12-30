package com.example.loveapp.ui.account

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.loveapp.MainActivity
import com.example.loveapp.R
import com.example.loveapp.data.Resource
import com.example.loveapp.databinding.FragmentLogInBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class LogInFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel
    private var _binding: FragmentLogInBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authViewModel =
            ViewModelProvider(this)[AuthViewModel::class.java]

        _binding = FragmentLogInBinding.inflate(inflater, container, false)

        authViewModel.navigate.observe(viewLifecycleOwner){
            if(it != null) {
                findNavController().navigate(R.id.action_LogInFragment_to_testFragment)
                authViewModel.finishNavigate()
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner){
            if(it != null) {
                Toast.makeText(this.context, it, Toast.LENGTH_LONG).show()
                authViewModel.completeErrorMessage()
            }
        }

        authViewModel.loginData.observe(viewLifecycleOwner){
            it?.let {
                when(it){
                    is Resource.Failure -> {
                        authViewModel.showErrorMessage(it.exception)
                        binding.buttonLogIn.visibility = View.VISIBLE
                        binding.progressbarLoading.visibility = View.GONE

                    }
                    Resource.Loading -> {
                        binding.buttonLogIn.visibility = View.INVISIBLE
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

        binding.buttonLogIn.setOnClickListener {
            authViewModel.login(
                binding.edittextEmail.text.toString(),
                binding.edittextPassword.text.toString()
            )
        }

        binding.textviewToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_SignUpFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}