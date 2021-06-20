package com.chardon.faceval.android.ui.shutter

import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chardon.faceval.android.util.LenFacing

class ShutterViewModel : ViewModel() {
    val selectorMap = mapOf(
        LenFacing.FRONT to CameraSelector.DEFAULT_FRONT_CAMERA,
        LenFacing.BACK to CameraSelector.DEFAULT_BACK_CAMERA,
//            LenFacing.UNKNOWN to null,
    )

    private val defaultLenFacing = LenFacing.FRONT

    private val _currentLenFacing = MutableLiveData(defaultLenFacing)
    val currentLenFacing: LiveData<LenFacing> = _currentLenFacing

    fun switch(): LenFacing {
        when (_currentLenFacing.value) {
            LenFacing.FRONT -> _currentLenFacing.value = LenFacing.BACK
            LenFacing.BACK -> _currentLenFacing.value = LenFacing.FRONT
            else -> return LenFacing.FRONT
        }

        return _currentLenFacing.value!!
    }
}