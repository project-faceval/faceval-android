package com.chardon.faceval.android.ui.recordlist

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordDetailViewModel : ViewModel() {

    private val _image = MutableLiveData<Bitmap>()
    val image: LiveData<Bitmap> = _image

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    fun updateInfo(image: Bitmap, title: String, description: String) {
        _image.value = image
        _title.value = title
        _description.value = description
    }
}