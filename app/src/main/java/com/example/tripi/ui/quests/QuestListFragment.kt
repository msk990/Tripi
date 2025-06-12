package com.example.tripi.ui.quests

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.GridLayoutManager
import com.example.tripi.databinding.FragmentQuestListBinding

class QuestListFragment :
    Fragment() {

    private lateinit var binding: FragmentQuestListBinding
    private lateinit var adapter: QuestAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQuestListBinding.inflate(inflater, container, false)

        val quests = QuestRepository.loadQuests(requireContext())
        Log.d("QuestListFragment", "Loaded ${quests.size} quests")

        adapter = QuestAdapter(quests) { quest ->
            val action = QuestListFragmentDirections.actionToQuestDetail(quest.id)
            findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter

        return binding.root
    }
}
