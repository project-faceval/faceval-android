package com.chardon.faceval.android.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithPhotoList (
    @Embedded
    val user: User,
    @Relation(parentColumn = "id", entityColumn = "user_id")
    val photos: List<Photo>
)