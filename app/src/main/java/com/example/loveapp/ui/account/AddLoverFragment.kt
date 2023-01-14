package com.example.loveapp.ui.account

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loveapp.MainActivity
import com.example.loveapp.R
import com.example.loveapp.data.Request
import com.example.loveapp.data.RequestCallbacks
import com.example.loveapp.databinding.FragmentAddLoverBinding
import com.google.android.material.snackbar.Snackbar

class AddLoverFragment : Fragment(), RequestCallbacks {
    private lateinit var addLoverViewModel: AddLoverViewModel
    private var _binding: FragmentAddLoverBinding? = null

    private lateinit var requestList: RecyclerView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addLoverViewModel =
            ViewModelProvider(this)[AddLoverViewModel::class.java]

        _binding = FragmentAddLoverBinding.inflate(inflater, container, false)

        requestList = binding.rvRequests
        requestList.adapter = AddLoverAdapter(this, emptyList())
        requestList.layoutManager = LinearLayoutManager(context)

        addLoverViewModel.isTaken.observe(viewLifecycleOwner) {
            if (it) {
                startActivity(Intent(this.context, MainActivity::class.java))
                addLoverViewModel.stopJob()
            }
        }

        addLoverViewModel.requests.observe(viewLifecycleOwner) {
            it?.let { requests ->
                (requestList.adapter as AddLoverAdapter).submitList(requests)
            }
        }

        addLoverViewModel.navBackToLogin.observe(viewLifecycleOwner) {
            if (it != null) {
                findNavController().navigate(R.id.action_addLoverFragment_to_LogInFragment)
                addLoverViewModel.onNavBackToLoginCompleted()
            }
        }

        addLoverViewModel.username.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.textviewGreetUser.text = resources.getString(R.string.greet_user, it)
            }
        }
        addLoverViewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it != null) {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                addLoverViewModel.completeErrorMessage()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAddLover.setOnClickListener {

            if (!TextUtils.isEmpty(binding.edittextLoverDay.text) &&
                !TextUtils.isEmpty(binding.edittextLoverMonth.text) &&
                !TextUtils.isEmpty(binding.edittextLoverMonth.text)
            ) {
                val date = listOf(
                    binding.edittextLoverDay.text.toString().toInt(),
                    binding.edittextLoverMonth.text.toString().toInt(),
                    binding.edittextLoverYear.text.toString().toInt()
                )
                addLoverViewModel.addLover(
                    binding.edittextLoverEmail.text.toString(),
                    date
                )
            } else {
                addLoverViewModel.showErrorMessage("Fill in the date.")
            }

        }
    }

    override fun onAccept(request: Request) {
        addLoverViewModel.acceptRequest(request)
    }

    override fun onDecline(request: Request) {
        addLoverViewModel.declineRequest(request)
    }

}