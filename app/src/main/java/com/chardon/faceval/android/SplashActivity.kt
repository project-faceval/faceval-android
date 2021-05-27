package com.chardon.faceval.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.*

/**
 * An activity showing startup splash while the app is initializing
 *
 * @author chardon
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    private suspend fun loadMainActivityAsync() {
        coroutineScope {
            launch {
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Load MainActivity asynchronously
        GlobalScope.launch {
            loadMainActivityAsync()
        }
    }

    // Suppress back button action during the splash showing
    override fun onBackPressed() {}
}