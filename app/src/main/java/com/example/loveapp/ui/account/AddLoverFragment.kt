package com.example.loveapp.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.loveapp.R
import com.example.loveapp.databinding.FragmentAddLoverBinding
import com.example.loveapp.ui.account.login.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddLoverFragment : Fragment() {
    private lateinit var authViewModel: AuthViewModel
    private var _binding: FragmentAddLoverBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authViewModel =
            ViewModelProvider(this)[AuthViewModel::class.java]

        _binding = FragmentAddLoverBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_testFragment_to_LogInFragment)
        }
    }

}