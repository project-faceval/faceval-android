package com.chardon.faceval.android.ui.scoring

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chardon.faceval.android.rest.client.AIClient
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.PhotoClient
import com.chardon.faceval.android.util.Action
import com.chardon.faceval.android.util.Base64Util.toBase64String
import com.chardon.faceval.android.util.MiscExtensions.convertForScoring
import com.chardon.faceval.android.util.MiscExtensions.toMap
import com.chardon.faceval.android.util.ScoringPhases
import com.chardon.faceval.entity.DetectionModelBase64
import com.chardon.faceval.entity.DetectionResult
import com.chardon.faceval.entity.PhotoInfo
import com.chardon.faceval.entity.PhotoInfoUploadBase64
import com.chardon.faceval.util.detectionmodelutils.Utils.toPosSet
import kotlinx.coroutines.*

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

    private val photoClient: PhotoClient by lazy {
        APISet.photoClient
    }

    fun startScoring(image: Bitmap) {
        if (_currentPhase.value != ScoringPhases.IDLE) {
            return
        }

        _currentImage.value = image
        _currentPhase.value = ScoringPhases.NOT_STARTED
    }

    suspend fun detect(): DetectionResult? {
        if (_currentImage.value == null) {
            return null
        }

        val base64 = _currentImage.value!!.toBase64String()
        currentImageBase64 = base64

        val model = DetectionModelBase64(
            ext = "png",
            bimg = base64,
            useBase64 = "yes",
        )

        var result: DetectionResult? = null

        try {
            result = aiClient.detectAsync(model.toMap()).await()
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }

        if (result != null) {
            detectionModel = model
            _positions.value = result
        }

        return result
    }

    fun finishDetection() {
        _currentPhase.value = ScoringPhases.DETECTED
    }

    suspend fun score(detectionResult: DetectionResult): List<Double> {
        if (currentImageBase64 == null || detectionModel == null) {
            return listOf()
        }

        var result = listOf<Double>()

        val map = detectionResult.convertForScoring(detectionModel!!).toMap()

        var retry = 5

        while ((retry--) > 0) {
            try {
                result = aiClient.scoreDetectedAsync(map)
                    .await()
            } catch (e: Exception) {
                Log.e("ERROR", e.toString())
                if (e.toString() == "timeout") {
                    continue
                }
            }
            
            break
        }

        _score.value = result

        return result
    }

    fun finishScoring() {
        _currentPhase.value = ScoringPhases.SCORED
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

    fun reset(before: Action = Action {  }) {
        before.invoke()
        cancel()
        _positions.value = null
        _score.value = null
        _currentImage.value = null
        _currentPhase.value = ScoringPhases.IDLE
        currentImageBase64 = null
        detectionModel = null
    }

    suspend fun upload(username: String, password: String): PhotoInfo? {
        val newPhoto = PhotoInfoUploadBase64(
            id = username,
            password = password,
            image = _currentImage.value!!.toBase64String(),
            ext = "png",
            positions = _positions.value!!.toPosSet(),
            score = _score.value!!.getOrElse(0) {7.0},
            title = null,
            description = null,
        )

        var photoInfo: PhotoInfo? = null

        try {
            photoInfo = photoClient.addPhotoAsync(newPhoto.toMap()).await()
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }

        return photoInfo
    }
}