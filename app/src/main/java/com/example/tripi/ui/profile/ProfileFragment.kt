package com.example.tripi.ui.profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tripi.databinding.FragmentProfileBinding
import com.example.tripi.R

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfileImage()
    }

    private fun loadProfileImage() {
        val assetManager = requireContext().assets
        val inputStream = assetManager.open("stickers/wolf.png")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        binding.profileImage.setImageBitmap(bitmap) // Uses ViewBinding now!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

