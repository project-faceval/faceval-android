package com.chardon.faceval.android.ui.scoring

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.marginStart
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.findFragment
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.databinding.ActivityScoringBinding
import com.chardon.faceval.android.ui.LoadingFragment
import com.chardon.faceval.android.util.NotificationUtil.darkPurple
import com.chardon.faceval.android.util.ScoringPhases
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScoringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoringBinding

    private lateinit var scoringViewModel: ScoringViewModel

    private lateinit var loadingFragment: LoadingFragment

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scoring)

        binding.scoreField.visibility = View.GONE
        binding.facePosView.visibility = View.GONE

        binding.confirmBtn.isEnabled = false

        scoringViewModel = ViewModelProvider(this).get(ScoringViewModel::class.java)
        binding.scoringViewModel = scoringViewModel
        binding.lifecycleOwner = this

        loadingFragment = binding.loading.findFragment()

        scoringViewModel.apply {
            currentImage.observe(this@ScoringActivity) {
                binding.capturedImageView.setImageBitmap(it)
            }

            positions.observe(this@ScoringActivity) {
                it.face[0].apply {
                    binding.facePosView.apply {
                        x = startX.toFloat()
                        y = startY.toFloat()

                        layoutParams.apply {
                            width = lengthX
                            height = lengthY
                        }

                        visibility = View.VISIBLE
                    }

                    binding.scoreField.apply {
                        x = startX.toFloat() + lengthX.toFloat() / 2 - width / 2
                        y = startY.toFloat() + lengthY.toFloat() / 2 - height / 2
                    }
                }
            }

            score.observe(this@ScoringActivity) {
                binding.scoreField.apply {
                    text = String.format("%.1f", it[0])
                    visibility = View.VISIBLE
                }
            }

            currentPhase.observe(this@ScoringActivity) {
                when (it) {
                    ScoringPhases.IDLE -> {
                        binding.loading.visibility = View.VISIBLE
                        loadingFragment.message = getString(R.string.prepare)
                    }
                    ScoringPhases.NOT_STARTED -> {
                        loadingFragment.message = getString(R.string.detecting)
                    }
                    ScoringPhases.DETECTED -> {
                        loadingFragment.message = getString(R.string.scoring)
                        binding.upperLoading.visibility = View.VISIBLE
                        GlobalScope.launch {
                            delay(100)
                            binding.loading.visibility = View.GONE
                        }
                    }
                    ScoringPhases.SCORED -> {
                        binding.upperLoading.visibility = View.GONE
                        scoringViewModel.complete()
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
            scoringViewModel.reset()
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}