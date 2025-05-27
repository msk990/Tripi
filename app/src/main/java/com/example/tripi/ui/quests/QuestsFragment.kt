package com.example.tripi.ui.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tripi.databinding.FragmentQuestsBinding
import com.example.tripi.ui.quests.QuestsViewModel

class QuestsFragment : Fragment() {

    private var _binding: FragmentQuestsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val questsViewModel =
            ViewModelProvider(this).get(QuestsViewModel::class.java)

        _binding = FragmentQuestsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textQuests
        questsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
