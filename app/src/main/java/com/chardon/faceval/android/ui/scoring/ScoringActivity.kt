package com.chardon.faceval.android.ui.scoring

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.chardon.faceval.android.R
import com.chardon.faceval.android.databinding.ActivityScoringBinding

class ScoringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoringBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scoring)

        binding.positionFrame.isVisible = false
    }
}