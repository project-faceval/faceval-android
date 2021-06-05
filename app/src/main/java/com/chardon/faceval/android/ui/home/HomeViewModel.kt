package com.chardon.faceval.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _buttonText = MutableLiveData<String>()

    val buttonText: LiveData<String>
        get() = _buttonText

    fun callCamera() {

    }
}