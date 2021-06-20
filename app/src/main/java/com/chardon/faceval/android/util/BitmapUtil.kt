package com.chardon.faceval.android.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream

object BitmapUtil {

    fun ByteArray.toBitmap(): Bitmap {
        return BitmapFactory.decodeByteArray(this, 0, this.size)
    }

    fun InputStream.toBitmap(): Bitmap {
        return BitmapFactory.decodeStream(this)
    }
}