package com.chardon.faceval.android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chardon.faceval.android.R
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.UserClient
import com.chardon.faceval.android.util.Action
import com.chardon.faceval.android.util.MiscExtensions.toMap
import com.chardon.faceval.android.util.FieldChecker
import com.chardon.faceval.entity.UserInfoUpload
import kotlinx.coroutines.*
import kotlin.Exception

class RegisterViewModel : ViewModel() {

    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerForm: LiveData<RegisterFormState> = _registerForm

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    private val userClient: UserClient by lazy {
        APISet.userClient
    }

    fun register(userInfoUpload: UserInfoUpload, callback: Action = Action {  }) {
        val registerJob = Job()
        val registerScope = CoroutineScope(Dispatchers.Main + registerJob)

        registerJob.invokeOnCompletion { callback.invoke() }

        registerScope.launch {
            try {
                val deferredJob = userClient.createUserAsync(userInfoUpload.toMap())
                val newUser = deferredJob.await()

                _registerResult.value = RegisterResult(success = UserInfoUpload(
                    id = newUser.id,
                    displayName = newUser.displayName,
                    password = userInfoUpload.password,
                    email = newUser.email,
                    status = newUser.status,
                    gender = newUser.gender,
                ))
            } catch (e: Exception) {
                println(e)
                _registerResult.value = RegisterResult(error = R.string.register_failed)
            } finally {
                registerJob.complete()
            }
        }
    }

    private var userNameCheckJob: CompletableJob? = null
    private var usernameCheckScope: CoroutineScope? = null

    fun registerDataChanged(userName: String, email: String,
                            displayName: String, password: String, confirmPassword: String) {
        val state = RegisterFormState(
            userNameError = if (FieldChecker.username(userName)) null else R.string.invalid_username,
            emailError = if (FieldChecker.email(email)) null else R.string.invalid_email,
            displayNameError = if (FieldChecker.displayName(displayName)) null else R.string.invalid_display,
            passwordError = if (FieldChecker.password(password)) null else R.string.invalid_password,
            confirmPasswordError = if (password == confirmPassword) null else R.string.invalid_confirm_password,
        )

        _registerForm.value = state

        if (userName.isNotBlank()) {

            synchronized(this) {
                usernameCheckScope?.cancel()
                userNameCheckJob?.cancel()

                userNameCheckJob = Job()
                usernameCheckScope = CoroutineScope(Dispatchers.Main + userNameCheckJob!!)
            }

            try {
                if (userNameCheckJob?.isCancelled == false) {
                    usernameCheckScope!!.launch {
                        if (!FieldChecker.usernameNotConflict(userName)) {
                            val previousState = _registerForm.value
                            previousState?.conflictUsername = R.string.conflict_username
                            _registerForm.value = previousState
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore exceptions
            }
        }
    }

    fun reset() {
        _registerForm.value = null
    }
}