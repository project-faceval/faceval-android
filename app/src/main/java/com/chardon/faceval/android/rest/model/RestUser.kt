package com.chardon.faceval.android.rest.model

import java.util.*

data class UserInfo(
    val id: String,
    var email: String,
    var displayName: String,
    var gender: Boolean?,
    var status: String?,
    val dateAdded: Date,
)

data class PostUser(
    val id: String,
    var email: String,
    var displayName: String,
    var gender: Boolean?,
    var status: String?,
)