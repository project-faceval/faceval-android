package com.chardon.faceval.android.ui.scoring

import android.graphics.Bitmap
import android.media.Image
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chardon.faceval.android.rest.client.AIClient
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.PhotoClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ScoringViewModel : ViewModel() {

    private val _positions = MutableLiveData<List<Double>>()
    val positions: LiveData<List<Double>>
        get() = _positions

    private val _score = MutableLiveData<Double>()
    val score: LiveData<Double>
        get() = _score

    private val aiClient: AIClient by lazy {
        APISet.aiClient
    }

    suspend fun updateFromImageAsync(image: Bitmap) {
        coroutineScope {
            launch {
            }
        }
    }
}