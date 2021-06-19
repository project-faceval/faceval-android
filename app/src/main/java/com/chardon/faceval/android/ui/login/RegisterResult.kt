package com.chardon.faceval.android.ui.login

import com.chardon.faceval.entity.UserInfoUpload

data class RegisterResult(
    val success: UserInfoUpload? = null,
    val error: Int? = null,
)