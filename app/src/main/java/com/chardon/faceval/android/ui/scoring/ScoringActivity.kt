package com.chardon.faceval.android.ui.scoring

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.marginStart
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.findFragment
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.LoginDataSource
import com.chardon.faceval.android.data.UserDatabase
import com.chardon.faceval.android.databinding.ActivityScoringBinding
import com.chardon.faceval.android.ui.LoadingFragment
import com.chardon.faceval.android.util.Action
import com.chardon.faceval.android.util.BitmapUtil.toBitmap
import com.chardon.faceval.android.util.NotificationUtil.darkPurple
import com.chardon.faceval.android.util.ScoringPhases
import com.chardon.faceval.entity.DetectionResult
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream

class ScoringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoringBinding

    private lateinit var scoringViewModel: ScoringViewModel

    private lateinit var messageField: TextView

    private lateinit var loginDataSource: LoginDataSource

    private val scoringJob = Job()
    private val scoringScope = CoroutineScope(Dispatchers.Main + scoringJob)

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scoring)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.scoreField.visibility = View.GONE
        binding.facePosView.visibility = View.GONE

        binding.confirmBtn.isEnabled = false

        scoringViewModel = ViewModelProvider(this).get(ScoringViewModel::class.java)
        binding.scoringViewModel = scoringViewModel
        binding.lifecycleOwner = this

        messageField = binding.loading.loadingPrompt

        loginDataSource = LoginDataSource(UserDatabase.getInstance(applicationContext).userDao)

        scoringViewModel.apply {
            currentImage.observe(this@ScoringActivity) {
                if (it == null) {
                    return@observe
                }

                binding.capturedImageView.setImageBitmap(it)
            }

            positions.observe(this@ScoringActivity) {
                if (it?.face == null) {
                    return@observe
                }

                if (it.face.isEmpty()) {
                    Toast.makeText(applicationContext, "No face detected!", Toast.LENGTH_LONG)
                    scoringViewModel.reset()
                    return@observe
                }

                it.face[0].apply {
                    binding.facePosView.apply {
                        x = (startX.toFloat() + lengthX.toFloat() / 2) * (binding.capturedImageView.width / scoringViewModel.currentImage.value!!.width)
                        y = (startY.toFloat() + lengthY.toFloat() / 2) * (binding.capturedImageView.height / scoringViewModel.currentImage.value!!.height) + lengthY

                        layoutParams.apply {
                            width = lengthX * (binding.capturedImageView.width / scoringViewModel.currentImage.value!!.width)
                            height = lengthY * (binding.capturedImageView.width / scoringViewModel.currentImage.value!!.width)
                        }

                        visibility = View.VISIBLE
                    }

                    binding.scoreField.apply {
                        x = 0.toFloat()
                        y = 0.toFloat()
                    }
                }
            }

            score.observe(this@ScoringActivity) {
                if (it == null || it.isEmpty()) {
                    return@observe
                }

                binding.scoreField.apply {
                    text = String.format("%.1f", it[0])
                    visibility = View.VISIBLE
                }
            }

            currentPhase.observe(this@ScoringActivity) {
                when (it) {
                    ScoringPhases.IDLE -> {
                        val path = intent.getStringExtra("image_path")
                        if (path == null) {
                            Toast.makeText(applicationContext, "No image!", Toast.LENGTH_LONG)
                                .show()
                            setResult(RESULT_CANCELED, intent)
                            finish()
                        }

                        binding.loading.root.visibility = View.VISIBLE
                        messageField.text = getString(R.string.prepare)

                        val bitmap: Bitmap

                        File(filesDir, path!!).apply {
                            FileInputStream(this).use { inputStream ->
                                bitmap = inputStream.toBitmap()
                            }
                        }

                        scoringScope.launch {
                            startScoring(bitmap)
                        }
                    }
                    ScoringPhases.NOT_STARTED -> {
                        messageField.text = getString(R.string.detecting)

                        scoringScope.launch {
                            val result = scoringViewModel.detect()
                            if (result == null) {
                                Toast.makeText(applicationContext, "No face detected", Toast.LENGTH_LONG)
                                    .show()
                                return@launch
                            }

                            scoringViewModel.finishDetection()
                        }
                    }
                    ScoringPhases.DETECTED -> {
                        messageField.text = getString(R.string.scoring)
                        binding.upperLoading.visibility = View.VISIBLE
                        binding.facePosView.visibility = View.VISIBLE
                        scoringScope.launch {
                            delay(100)
                            binding.loading.root.visibility = View.GONE
                        }

                        if (scoringViewModel.positions.value == null) {
                            return@observe
                        }

                        scoringScope.launch {
                            val result = scoringViewModel.score(scoringViewModel.positions.value!!)
                            if (result.isEmpty()) {
                                return@launch
                            }

                            scoringViewModel.finishScoring()
                        }
                    }
                    ScoringPhases.SCORED -> {
                        scoringScope.launch {
                            binding.upperLoading.visibility = View.GONE
                            binding.scoreField.visibility = View.VISIBLE
                            scoringViewModel.complete()
                        }
                    }
                    ScoringPhases.FINISHED -> {
                        Snackbar.make(binding.root, R.string.finished, Snackbar.LENGTH_SHORT)
                            .darkPurple()
                            .show()
                        binding.confirmBtn.isEnabled = true
                    }
                    ScoringPhases.CANCELED -> {
                        // Nothing to do (yet...)
                    }
                    ScoringPhases.ERROR -> {
                        Snackbar.make(binding.root, "Error occurred", Snackbar.LENGTH_LONG)
                            .darkPurple()
                            .show()
                    }
                    else -> {}
                }
            }
        }

        binding.cancelBtn.setOnClickListener {
            scoringViewModel.reset {
                scoringViewModel.currentPhase.removeObservers(this)
            }
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        binding.confirmBtn.setOnClickListener {
            messageField.text = getString(R.string.uploading)
            binding.loading.root.visibility = View.VISIBLE
            upload {
                scoringViewModel.reset {
                    scoringViewModel.currentPhase.removeObservers(this)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    @SuppressLint("ShowToast")
    fun upload(callback: Action = Action {  }) {
        val uploadJob = Job()
        val uploadScope = CoroutineScope(Dispatchers.Main + uploadJob)

        uploadJob.invokeOnCompletion {
            callback.invoke()
        }

        uploadScope.launch {
            val user = loginDataSource.getCurrentUser()
            if (user == null) {
                Snackbar.make(binding.root, "Cannot get current user", Snackbar.LENGTH_LONG)
                    .darkPurple()
                    .show()
                return@launch
            }

            val photoInfo = scoringViewModel.upload(user.id, user.password ?: "")
            if (photoInfo == null) {
                Snackbar.make(binding.root, "Unexpected error", Snackbar.LENGTH_LONG)
                    .darkPurple()
                    .show()
                return@launch
            }

            intent.putExtra("score", photoInfo.score)
            intent.putExtra("positions", photoInfo.positions)

            uploadJob.complete()
        }
    }
}