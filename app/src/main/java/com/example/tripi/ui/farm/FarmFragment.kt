package com.example.tripi.ui.farm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tripi.databinding.FragmentFarmBinding
import com.example.tripi.ui.farm.FarmViewModel

class FarmFragment : Fragment() {

    private var _binding: FragmentFarmBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val farmViewModel =
            ViewModelProvider(this).get(FarmViewModel::class.java)

        _binding = FragmentFarmBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textFarm
        farmViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
