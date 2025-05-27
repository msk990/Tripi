package com.example.tripi.ui.farm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FarmViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is farm Fragment"
    }
    val text: LiveData<String> = _text
}
