package com.chardon.faceval.android.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object Base64Util {
    
    fun Bitmap.toBase64String(): String {
        val imageBytes: ByteArray

        ByteArrayOutputStream().use {
            this.compress(Bitmap.CompressFormat.PNG, 100, it)
            imageBytes = it.toByteArray()
        }

        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    fun Bitmap.toBase64(): ByteArray {
        val imageBytes: ByteArray

        ByteArrayOutputStream().use {
            this.compress(Bitmap.CompressFormat.PNG, 100, it)
            imageBytes = it.toByteArray()
        }

        return Base64.encode(imageBytes, Base64.DEFAULT)
    }

    fun String.base64ToBitmap(): Bitmap {
        val imageBytes = Base64.decode(this, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}