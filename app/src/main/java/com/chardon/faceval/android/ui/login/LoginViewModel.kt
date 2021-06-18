package com.chardon.faceval.android.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.LoginDataSource
import com.chardon.faceval.android.data.LoginRepository
import com.chardon.faceval.android.data.Result
import com.chardon.faceval.android.data.dao.UserDao
import com.chardon.faceval.entity.UserInfo
import kotlinx.coroutines.*
import java.util.regex.Pattern

class LoginViewModel(private val userDao: UserDao,
                     application: Application) : AndroidViewModel(application) {

    // Kotlin coroutine definitions
    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    // End

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val loginDataSource: LoginDataSource = LoginDataSource(userDao)

    private val _loginRepository: LoginRepository = LoginRepository(loginDataSource)
    val loginRepository: LoginRepository = _loginRepository

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        _loginRepository.isInitialized.apply {
            if (value == true) {
                executeLogin(username, password)
            } else {
                observe(getApplication()) { value ->
                    if (value) {
                        executeLogin(username, password)
                        removeObserver(getApplication())
                    }
                }
            }
        }
    }

    private fun executeLogin(username: String, password: String) {
        uiScope.launch {
            val result = _loginRepository.login(username, password)

            if (result is Result.Success) {
                val view = LoggedInUserView(
                    displayName = result.data.displayName,
                    email = result.data.email,
                    gender = result.data.gender,
                    status = result.data.status,
                    userId = result.data.id,
                )

                _loginResult.value = LoginResult(success = view)
            } else {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun logout() = _loginRepository.logout()

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return Pattern.matches("^[^\\s]+$", username)
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}