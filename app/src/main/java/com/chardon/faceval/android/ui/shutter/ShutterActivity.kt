package com.chardon.faceval.android.ui.shutter

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.databinding.ActivityShutterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

class ShutterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShutterBinding

    private lateinit var shutterViewModel: ShutterViewModel

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var imageCapture: ImageCapture

    private var permissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shutter)

        shutterViewModel = ViewModelProvider(this).get(ShutterViewModel::class.java)
        binding.shutterViewModel = shutterViewModel
        binding.lifecycleOwner = this

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            val front = CameraSelector.DEFAULT_FRONT_CAMERA
            val back = CameraSelector.DEFAULT_BACK_CAMERA

            var cameraSelector: CameraSelector = front

            when {
                cameraProvider.hasCamera(front) -> {
                    cameraSelector = front
                }
                cameraProvider.hasCamera(back) -> {
                    cameraSelector = back
                }
                else -> {
                    Toast.makeText(applicationContext, "Your phone has no cameras!", Toast.LENGTH_LONG)
                         .show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.root.display.rotation)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                AlertDialog.Builder(applicationContext)
                    .setTitle("Cannot load camera")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))

        binding.apply {
            cancelCameraButton.setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            takePhotoButton.setOnClickListener {
                capture()
            }

            switchCameraButton.setOnClickListener {
                Snackbar.make(binding.root, "Switch not supported yet", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun capture() {
        ByteArrayOutputStream().use {
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(it).build()
            imageCapture.takePicture(outputFileOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        AlertDialog.Builder(applicationContext)
                            .setTitle("Image capture failed")
                            .show()
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val byteArray = it.toByteArray()
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                        Toast.makeText(applicationContext, "Image captured successfully", Toast.LENGTH_LONG)
                             .show()

                        intent.putExtra("bitmap", bitmap)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        ensureCameraPermission()
    }

    private fun ensureCameraPermission() {
        if (permissionRequested) {
            return
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), 0)
        }

        permissionRequested = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            0 -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext,
                        "Faceval Camera cannot work properly: camera permission is not granted",
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
            else -> {}
        }
    }
}