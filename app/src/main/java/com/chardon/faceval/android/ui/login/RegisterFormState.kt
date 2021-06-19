package com.chardon.faceval.android.ui.login

data class RegisterFormState(
    val userNameError: Int? = null,
    val emailError: Int? = null,
    val displayNameError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,
    var conflictUsername: Int? = null,
) {
    val isDataValid: Boolean
        get() =
            (userNameError ?: emailError ?: displayNameError ?: passwordError ?: conflictUsername ?: conflictUsername) == null
}
