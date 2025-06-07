package com.example.tripi.ui.wallet

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripi.R
import com.example.tripi.storage.StickerRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider


class WalletFragment : Fragment(

) {
    private val viewModel: WalletViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)
        val pointsText = view.findViewById<TextView>(R.id.totalPointsTextView)
        val purchasesList = view.findViewById<RecyclerView>(R.id.recentPurchasesRecyclerView)
        val viewModel = ViewModelProvider(this)[WalletViewModel::class.java]


        viewModel.points.observe(viewLifecycleOwner) { points ->
            pointsText.text = "$points Points"
        }
        viewModel.loadPoints()


        purchasesList.layoutManager = LinearLayoutManager(requireContext())
        purchasesList.adapter = MockPurchaseAdapter(
            listOf(
                "10 Points - Quest reward",
                "15 Points - Scanned bicycle",
                "5 Points - Bonus streak"
            )
        )

        return view
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadPoints()
    }

}
