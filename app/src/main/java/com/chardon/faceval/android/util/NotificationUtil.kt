package com.chardon.faceval.android.util

import android.widget.FrameLayout
import com.chardon.faceval.android.R
import com.google.android.material.snackbar.Snackbar

object NotificationUtil {

    fun Snackbar.setGravity(gravity: Int): Snackbar {
        val view = this.view
        val params = view.layoutParams as (FrameLayout.LayoutParams)
        params.gravity = gravity
        return this
    }

    fun Snackbar.darkPurple(): Snackbar {
        val view = this.view
        view.setBackgroundResource(R.color.purple_900)

        return this
    }
}