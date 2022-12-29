package com.example.loveapp.ui.account

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.loveapp.R
import com.example.loveapp.databinding.FragmentSignUpBinding
import com.example.loveapp.databinding.FragmentTestBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestFragment : Fragment() {
    private lateinit var authViewModel: AuthViewModel
    private var _binding: FragmentTestBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authViewModel =
            ViewModelProvider(this)[AuthViewModel::class.java]

        _binding = FragmentTestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textviewUsername.text = authViewModel.currentUser?.displayName
        binding.button.setOnClickListener {
            findNavController().navigate(R.id.action_testFragment_to_LogInFragment)
            authViewModel.logout()
        }
    }

}