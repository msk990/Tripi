package com.example.tripi.ui.wallet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripi.storage.StickerRepository
import kotlinx.coroutines.launch

class WalletViewModel : ViewModel() {

    private val _points = MutableLiveData<Int>()
    val points: LiveData<Int> = _points

    fun loadPoints() {
        viewModelScope.launch {
            val total = StickerRepository.getPoints()
            Log.d("WalletVM", "Fetched points: $total")
            _points.postValue(total)
        }

    }


    fun incrementPoints(amount: Int) {
        viewModelScope.launch {
            StickerRepository.addPoints(amount)
            loadPoints() // Refresh after updating
        }
    }
}
