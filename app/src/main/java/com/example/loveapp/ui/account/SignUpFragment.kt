package com.example.loveapp.ui.account

import android.os.Bundle
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
                findNavController().navigate(R.id.action_SignUpFragment_to_LogInFragment)
                authViewModel.finishNavigate()
            }
        }

        authViewModel.signupData.observe(viewLifecycleOwner){
            it?.let {
                when(it){
                    is Resource.Failure -> {
                        Toast.makeText(this.context, "Failed sign up", Toast.LENGTH_LONG).show()
                    }
                    Resource.Loading -> {
                        Toast.makeText(this.context, "signing up", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Success -> {
                        binding.textviewTestdata.text = authViewModel.currentUser?.displayName
                        //authViewModel.startNavigate()
                    }
                }
            }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignUp.setOnClickListener {
            authViewModel.signup(
                binding.edittextUsername.text.toString(),
                binding.edittextEmail.text.toString(),
                binding.edittextPassword.text.toString()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}