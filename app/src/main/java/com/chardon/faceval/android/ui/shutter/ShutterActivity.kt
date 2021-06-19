package com.chardon.faceval.android.ui.shutter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.databinding.ActivityShutterBinding

class ShutterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShutterBinding

    private lateinit var shutterViewModel: ShutterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shutter)

        shutterViewModel = ViewModelProvider(this).get(ShutterViewModel::class.java)
        binding.shutterViewModel = shutterViewModel
        binding.lifecycleOwner = this
    }
}