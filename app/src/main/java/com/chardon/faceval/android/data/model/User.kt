package com.chardon.faceval.android.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "user")
data class User (
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "email")
    var email: String,
    @ColumnInfo(name = "password")
    var password: String?,
    @ColumnInfo(name = "date_joined")
    val dateJoined: String?,
    @ColumnInfo(name = "display_name")
    var displayName: String,
    @ColumnInfo(name = "gender")
    var gender: Boolean? = null,
    @ColumnInfo(name = "status")
    var status: String? = null,
    @ColumnInfo(name = "is_active")
    var isActive: Boolean = true,
)