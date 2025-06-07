package com.example.tripi.ui.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripi.R
import com.example.tripi.stickers.model.StickerAssetMap
import com.example.tripi.storage.StickerRepository
import com.example.tripi.ui.collection.adapter.StickerCollectionAdapter


import kotlinx.coroutines.launch

class StickerCollectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sticker_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.stickerRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        lifecycleScope.launch {
            val collectedLabels = StickerRepository.getCollected().map { it.label }.toSet()
            val allStickers = StickerAssetMap.loadStickerData(requireContext())

            val items = collectedLabels.mapNotNull { label ->
                allStickers[label]?.let { info -> label to info.image }
            }

            recyclerView.adapter = StickerCollectionAdapter(requireContext(), items)
        }


    }
}
