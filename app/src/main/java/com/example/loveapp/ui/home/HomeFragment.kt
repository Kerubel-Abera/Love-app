package com.example.loveapp.ui.home

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.loveapp.R
import com.example.loveapp.databinding.FragmentHomeBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private var user: Int = -1

    private lateinit var milestonesList: RecyclerView

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

        milestonesList = binding.rvMilestones
        milestonesList.adapter = HomeMilestonesAdapter(emptyList())
        milestonesList.layoutManager = LinearLayoutManager(context)

        homeViewModel.firstUserIcon.observe(viewLifecycleOwner){ uri ->
            val imageView = binding.imageviewFirstPerson
            Glide.with(this)
                .load(uri)
                .error(R.drawable.ic_add_photo)
                .transform(CenterCrop(), RoundedCorners(11))
                .into(imageView)
        }

        homeViewModel.secondUserIcon.observe(viewLifecycleOwner){ uri ->
            val imageView = binding.imageviewSecondPerson
            Glide.with(this)
                .load(uri)
                .error(R.drawable.ic_add_photo)
                .transform(CenterCrop(), RoundedCorners(11))
                .into(imageView)
        }

        homeViewModel.milestones.observe(viewLifecycleOwner){ milestones ->
            milestones?.let { milestones ->
                (milestonesList.adapter as HomeMilestonesAdapter).submitList(milestones)
            }
        }

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
                homeViewModel.getMilestones()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val openGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null && user != -1) {
                    val intent = context?.let {
                        CropImage.activity(imageUri)
                            .setAspectRatio(1, 1)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(it, this)
                    }
                }
            }
        }

        binding.imageviewFirstPerson.setOnClickListener {
            user = 1
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            openGallery.launch(intent)
        }

        binding.imageviewSecondPerson.setOnClickListener {
            user = 2
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            openGallery.launch(intent)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val test = CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                homeViewModel.postUserIcon(user, resultUri)
                GlobalScope.launch {
                    delay(2000L)
                    withContext(Dispatchers.Main) {
                        homeViewModel.updateUserIcons()
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                // Handle crop error
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}