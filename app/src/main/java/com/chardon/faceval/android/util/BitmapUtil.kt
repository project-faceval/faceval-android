package com.chardon.faceval.android.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import com.chardon.faceval.android.util.BitmapUtil.save
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
    private const val THUMBNAIL_SIZE = 280

    fun Bitmap.scaleTo(thumbnail: Boolean = false): Bitmap {
        val newWidth: Int
        val newHeight: Int

        val s = if (thumbnail) THUMBNAIL_SIZE else TARGET_SIZE

        if (width > height) {
            newHeight = s
            newWidth = s * width / height
        } else {
            newWidth = s
            newHeight = s * height / width
        }

        return this.scale(newWidth, newHeight)
    }

    fun Bitmap.save(outputStream: FileOutputStream, thumbnail: Boolean = false) {
        ByteArrayOutputStream().use {
            this.scaleTo(thumbnail)
                .compress(Bitmap.CompressFormat.PNG, 100, it)
            outputStream.write(it.toByteArray())
        }
    }
}