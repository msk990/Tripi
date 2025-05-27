package com.example.tripi.ui.quests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QuestsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is quests Fragment"
    }
    val text: LiveData<String> = _text
}
