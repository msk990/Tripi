package com.example.tripi.ui.ar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tripi.databinding.FragmentArBinding

class ArFragment : Fragment() {

    private var _binding: FragmentArBinding? = null
    private val binding get() = _binding!!

    private var markerName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If using SafeArgs:
        markerName = arguments?.let {
            ArFragmentArgs.fromBundle(it).markerName
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.arPlaceholderText.text = "AR Mode for: ${markerName ?: "Unknown"}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
