package com.chardon.faceval.android.ui.scoring

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chardon.faceval.android.rest.client.AIClient
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.PhotoClient
import com.chardon.faceval.android.util.Base64Util.toBase64String
import com.chardon.faceval.android.util.MiscExtensions.convertForScoring
import com.chardon.faceval.android.util.MiscExtensions.toMap
import com.chardon.faceval.android.util.ScoringPhases
import com.chardon.faceval.entity.DetectionModelBase64
import com.chardon.faceval.entity.DetectionResult
import kotlinx.coroutines.*
import okhttp3.MultipartBody

class ScoringViewModel : ViewModel() {
    private val _positions = MutableLiveData<DetectionResult>()
    val positions: LiveData<DetectionResult>
        get() = _positions

    private val _score = MutableLiveData<List<Double>>()
    val score: LiveData<List<Double>>
        get() = _score

    private val _currentImage = MutableLiveData<Bitmap>()
    val currentImage: LiveData<Bitmap> = _currentImage

    private var currentImageBase64: String? = null

    private var detectionModel: DetectionModelBase64? = null

    private val _currentPhase = MutableLiveData(ScoringPhases.IDLE)
    val currentPhase: LiveData<ScoringPhases> = _currentPhase

    private var scoringJob = Job()
    private var scoringScope = CoroutineScope(Dispatchers.Main + scoringJob)

    private var detectJob = Job()
    private var detectScope = CoroutineScope(Dispatchers.Main + detectJob)

    private val aiClient: AIClient by lazy {
        APISet.aiClient
    }

//    init {
//        reset()
//    }

    fun startScoring(image: Bitmap) {
        if (_currentPhase.value != ScoringPhases.IDLE) {
            return
        }

        _currentPhase.value = ScoringPhases.NOT_STARTED

        scoringJob.invokeOnCompletion {
            _currentPhase.value = ScoringPhases.SCORED
        }

        detectJob.invokeOnCompletion {
            _currentPhase.value = ScoringPhases.DETECTED

            scoringScope.launch {
                _score.value = _positions.value?.let { it1 -> score(it1) }
                scoringJob.complete()
            }
        }


        detectScope.launch {
            _positions.value = detect()
            detectJob.complete()
        }
    }

    private suspend fun detect(): DetectionResult? {
        if (_currentImage.value == null) {
            return null
        }

        val base64 = _currentImage.value!!.toBase64String()
        currentImageBase64 = base64

        val model = DetectionModelBase64(
            ext = "png",
            bimg = base64,
        )

        var result: DetectionResult? = null

        try {
            result = aiClient.detectAsync(model.toMap()).await()
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }

        detectionModel = model

        return result
    }

    private suspend fun score(detectionResult: DetectionResult): List<Double> {
        if (currentImageBase64 == null || detectionModel == null) {
            return listOf()
        }

        var result = listOf<Double>()

        try {
            result = aiClient.scoreDetectedAsync(
                detectionResult.convertForScoring(detectionModel!!).toMap())
                .await()
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }

        return result
    }

    fun complete() {
        _currentPhase.value = ScoringPhases.FINISHED
    }

    fun cancel() {
        detectScope.cancel()
        detectJob.cancel()
        scoringScope.cancel()
        scoringJob.cancel()

        scoringJob = Job()
        scoringScope = CoroutineScope(Dispatchers.Main + scoringJob)

        detectJob = Job()
        detectScope = CoroutineScope(Dispatchers.Main + detectJob)

        _currentPhase.value = ScoringPhases.CANCELED
    }

    @Synchronized
    fun reset() {
        cancel()
        _positions.value = null
        _score.value = null
        _currentImage.value = null
        _currentPhase.value = ScoringPhases.IDLE
        currentImageBase64 = null
        detectionModel = null
    }
}