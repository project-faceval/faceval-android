package com.chardon.faceval.android.data.model

import java.util.*

data class Record (
    val userId: String,
    val photo: Photo,
    var title: String?,
    var description: String?,
    val addDate: Date,
)