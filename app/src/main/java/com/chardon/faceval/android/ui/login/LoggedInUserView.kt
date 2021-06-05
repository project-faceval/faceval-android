package com.chardon.faceval.android.ui.login

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val userId: String,
    val displayName: String,
    var email: String,
    var gender: Boolean? = null,
    var status: String? = null,
)