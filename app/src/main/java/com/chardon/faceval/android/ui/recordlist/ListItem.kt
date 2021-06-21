package com.chardon.faceval.android.ui.recordlist

import android.graphics.Bitmap
import java.util.*

data class ListItem(
//    val imageSrc: String,
    val photoId: Long,
    val bitmap: Bitmap?,
    val title: String = "No title",
    val score: Double,
    val dateAdded: Date,
) {
    override fun toString(): String = "$title,$dateAdded"
}
