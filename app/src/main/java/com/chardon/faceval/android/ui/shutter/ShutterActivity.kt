package com.chardon.faceval.android.ui.shutter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.chardon.faceval.android.util.Action
import com.chardon.faceval.android.util.BitmapUtil.save
import com.chardon.faceval.android.util.BitmapUtil.toBitmap
import com.chardon.faceval.android.util.FileNames
import com.chardon.faceval.android.util.LenFacing
import com.chardon.faceval.android.util.NotificationUtil.darkPurple
import com.chardon.faceval.android.util.NotificationUtil.setGravity
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ShutterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShutterBinding

    private lateinit var shutterViewModel: ShutterViewModel

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var imageCapture: ImageCapture

    private var permissionRequested = false

    private var notAllSupported = false

    private lateinit var currentLenFacingString: String
    private lateinit var frontString: String
    private lateinit var backString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shutter)

        shutterViewModel = ViewModelProvider(this).get(ShutterViewModel::class.java)
        binding.shutterViewModel = shutterViewModel
        binding.lifecycleOwner = this

        currentLenFacingString = getString(R.string.current_len_facing)
        frontString = getString(R.string.camera_front)
        backString = getString(R.string.camera_back)

        switchLenFacing(shutterViewModel.currentLenFacing.value!!, true)

        binding.apply {
            cancelCameraButton.setOnClickListener {
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
            }

            takePhotoButton.setOnClickListener {
                capture()
            }
        }

        shutterViewModel.currentLenFacing.observe(this) {
            switchLenFacing(it)
        }
    }

    private fun capture() {
        binding.loading.root.visibility = View.VISIBLE

        ByteArrayOutputStream().use {
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(it).build()
            imageCapture.takePicture(outputFileOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        AlertDialog.Builder(applicationContext)
                            .setTitle(R.string.capture_failed)
                            .show()

                        binding.loading.root.visibility = View.GONE
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val provider = cameraProviderFuture.get()
                        provider.unbindAll()

                        binding.loading.loadingPrompt.text = getString(R.string.saving)

                        val byteArray = it.toByteArray()

                        Toast.makeText(applicationContext, R.string.capture_success, Toast.LENGTH_LONG)
                             .show()

                        File(applicationContext.filesDir, FileNames.RAW_IMG).apply {
                            if (exists()) {
                                delete()
                            }

                            createNewFile()

                            FileOutputStream(this).use { fos ->
                                byteArray.toBitmap().save(fos)
                            }
                        }

                        endActivity {
                            intent.putExtra("image", FileNames.RAW_IMG)
                        }
                    }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.camera_top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (notAllSupported) {
            Snackbar.make(binding.root, "Switch not supported", Snackbar.LENGTH_SHORT).show()
            return false
        }

        return when (item.itemId) {
            R.id.switchMenuItem -> {
                shutterViewModel.switch().let {
                    val str = when (it) {
                        LenFacing.BACK -> backString
                        LenFacing.FRONT -> frontString
                        else -> ""
                    }

                    Snackbar.make(binding.root, currentLenFacingString.format(str), Snackbar.LENGTH_SHORT)
                        .darkPurple()
                        .setGravity(Gravity.TOP)
                        .show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun switchLenFacing(lenFacing: LenFacing, checkCamera: Boolean = false) {
        var actualLenFacing = lenFacing

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            if (checkCamera) {
                notAllSupported = false

                var cameraExists = false
                var defaultExists = false

                for (entry in shutterViewModel.selectorMap) {
                    if (cameraProvider.hasCamera(entry.value)) {
                        cameraExists = true

                        if (entry.key == shutterViewModel.currentLenFacing.value) {
                            defaultExists = true
                        }
                    } else {
                        notAllSupported = true
                    }
                }

                if (!cameraExists) {
                    endActivity(RESULT_CANCELED) {
                        Toast.makeText(
                            applicationContext,
                            R.string.camera_not_exists,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                if (!defaultExists && shutterViewModel.currentLenFacing.value == lenFacing) {
                    actualLenFacing = when (lenFacing) {
                        LenFacing.FRONT -> {
                            LenFacing.BACK
                        }
                        LenFacing.BACK -> {
                            LenFacing.FRONT
                        }
                        else -> {
                            return@addListener
                        }
                    }
                }
            }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.root.display.rotation)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,
                    shutterViewModel.selectorMap[actualLenFacing]!!, preview, imageCapture)
            } catch (e: Exception) {
                AlertDialog.Builder(applicationContext)
                    .setTitle(R.string.camera_load_failed)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        setResult(Activity.RESULT_CANCELED, intent)
                        finish()
                    }
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
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
                    endActivity(RESULT_CANCELED) {
                        Toast.makeText(applicationContext,
                            R.string.camera_no_permission,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            else -> {}
        }
    }

    private fun endActivity(resultCode: Int = RESULT_OK, before: Action = Action {  }) {
        before.invoke()
        setResult(resultCode, intent)
        finish()
    }
}