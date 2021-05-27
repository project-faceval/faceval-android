package com.chardon.faceval.android.data.model

import java.util.*

data class Photo(
    val id: String,
    val localPath: String,
    val score: Short?,
    val addDate: Date,
    val tag: String,
)
