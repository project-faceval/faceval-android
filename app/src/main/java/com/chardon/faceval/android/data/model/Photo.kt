package com.chardon.faceval.android.data.model

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "photo")
data class Photo(
    @PrimaryKey
    @ColumnInfo(name = "uuid_id")
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "picture")
    val picture: Bitmap,
    @ColumnInfo(name = "score")
    val score: Short?,
    @ColumnInfo(name = "face_positions")
    val facePositions: String,
    @ColumnInfo(name = "title")
    var title: String? = null,
    @ColumnInfo(name = "description")
    var description: String? = null,
    @ColumnInfo(name = "date_added")
    val dateAdded: Date,
)
