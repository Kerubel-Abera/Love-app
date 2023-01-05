package com.example.loveapp.ui.account.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.loveapp.MainActivity
import com.example.loveapp.R
import com.example.loveapp.data.Resource
import com.example.loveapp.databinding.FragmentLogInBinding
import com.google.android.material.snackbar.Snackbar
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

        authViewModel.isTaken.observe(viewLifecycleOwner) {
            when(it){
                true -> {
                    startActivity(Intent(this.context, MainActivity::class.java))
                    authViewModel.finishTakenUserCheck()
                    }
                false -> {
                    findNavController().navigate(R.id.action_LogInFragment_to_addLoverFragment)
                    authViewModel.finishTakenUserCheck()
                }
                else -> {}
            }
        }

        //Error Livedata
        authViewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it != null) {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                authViewModel.completeErrorMessage()
            }
        }


        //When user logs in this code will show the error, loading bar or start the navigation
        authViewModel.loginData.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
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
                        authViewModel.checkTakenUser()
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

        //sign up text clicklistener
        binding.textviewToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_SignUpFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}