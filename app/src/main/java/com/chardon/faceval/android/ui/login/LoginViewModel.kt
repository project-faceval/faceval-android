package com.chardon.faceval.android.ui.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import com.chardon.faceval.android.data.LoginRepository
import com.chardon.faceval.android.data.Result

import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.LoginDataSource
import com.chardon.faceval.android.data.UserDatabase

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val userDatabase: UserDatabase = Room.databaseBuilder(
        getApplication<Application>().applicationContext,
        UserDatabase::class.java, "faceval").build()

    private val loginDataSource: LoginDataSource = LoginDataSource(userDatabase.userDao())

    private val _loginRepository: LoginRepository = LoginRepository(loginDataSource)
    val loginRepository: LoginRepository
        get() = _loginRepository

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = _loginRepository.login(username, password)

        if (result is Result.Success) {
            val view = LoggedInUserView(
                displayName = result.data.displayName,
                email = result.data.email,
                gender = result.data.gender,
                status = result.data.status,
                userId = result.data.id,
            )

            _loginResult.value =
                LoginResult(success = view)
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
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

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return true
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}