package com.chardon.faceval.android.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream

object BitmapUtil {

    fun ByteArray.toBitmap(): Bitmap {
        return BitmapFactory.decodeByteArray(this, 0, this.size)
    }

    fun InputStream.toBitmap(): Bitmap {
        return BitmapFactory.decodeStream(this)
    }

    private const val TARGET_SIZE = 800

    fun Bitmap.save(outputStream: FileOutputStream) {
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newHeight = TARGET_SIZE
            newWidth = TARGET_SIZE * width / height
        } else {
            newWidth = TARGET_SIZE
            newHeight = TARGET_SIZE * height / width
        }

        ByteArrayOutputStream().use {
            this.scale(newWidth, newHeight)
                .compress(Bitmap.CompressFormat.PNG, 100, it)
            outputStream.write(it.toByteArray())
        }
    }
}