package com.example.tripi.ui.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NetworkViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is network Fragment"
    }
    val text: LiveData<String> = _text
}