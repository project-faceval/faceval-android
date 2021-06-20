package com.chardon.faceval.android.ui.scoring

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.databinding.ActivityScoringBinding

class ScoringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoringBinding

    private lateinit var scoringViewModel: ScoringViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scoring)

        binding.positionFrame.visibility = View.GONE
        binding.scoreField.visibility = View.GONE

        scoringViewModel = ViewModelProvider(this).get(ScoringViewModel::class.java)
        binding.scoringViewModel = scoringViewModel
        binding.lifecycleOwner = this


    }
}