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

        authViewModel.loginData.observe(viewLifecycleOwner){
            it?.let {
                when(it){
                    is Resource.Failure -> {
                        Toast.makeText(this.context, "Failed login", Toast.LENGTH_LONG).show()
                    }
                    Resource.Loading -> {
                        Toast.makeText(this.context, "Logging in", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Success -> {
                        binding.textviewTestdata.text = authViewModel.currentUser?.displayName
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}