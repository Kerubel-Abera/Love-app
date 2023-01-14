package com.example.loveapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.loveapp.R
import com.example.loveapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel.coupleData.observe(viewLifecycleOwner){ couple ->
            if(couple != null) {
                binding.textviewFirstPersonName.text = couple.firstPerson
                binding.textviewSecondPersonName.text = couple.secondPerson
                binding.textviewDate.text = resources.getString( R.string.date,
                    couple.date?.get(0) ?: 0,
                    couple.date?.get(1) ?: 0,
                    couple.date?.get(2) ?: 0
                )
                couple.date?.let { homeViewModel.getAmountOfDays(it) }
            }
        }

        homeViewModel.passedDays.observe(viewLifecycleOwner){ days ->
            if(days != null) {
                binding.textviewDays.text = resources.getString( R.string.days,
                    days)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}