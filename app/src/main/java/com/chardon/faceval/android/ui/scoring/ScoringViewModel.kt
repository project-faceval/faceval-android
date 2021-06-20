package com.chardon.faceval.android.ui.scoring

import android.graphics.Bitmap
import android.media.Image
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chardon.faceval.android.rest.client.AIClient
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.PhotoClient
import com.chardon.faceval.android.util.ScoringPhases
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

    private val _currentImage = MutableLiveData<Bitmap>()
    val currentImage: LiveData<Bitmap> = _currentImage

    private val _currentPhase = MutableLiveData(ScoringPhases.IDLE)
    val currentPhase: LiveData<ScoringPhases> = _currentPhase

    private val aiClient: AIClient by lazy {
        APISet.aiClient
    }

    fun startScoring(image: Bitmap) {
        if (_currentPhase.value != ScoringPhases.IDLE) {
            return
        }

        _currentPhase.value = ScoringPhases.NOT_STARTED
    }

    fun reset() {
        _positions.value = null
        _score.value = null
        _currentImage.value = null
        _currentPhase.value = ScoringPhases.IDLE
    }
}