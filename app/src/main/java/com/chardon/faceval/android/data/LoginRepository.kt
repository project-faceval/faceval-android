package com.chardon.faceval.android.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chardon.faceval.entity.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.sql.Date

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(private val dataSource: LoginDataSource) {

    private var repoJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + repoJob)

    // in-memory cache of the loggedInUser object
    var user: UserInfo? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    private val _isInitialized = MutableLiveData<Boolean>()
    val isInitialized: LiveData<Boolean>
        get() = _isInitialized

    suspend fun initialize() {
        val currentUser = dataSource.getCurrentUser()

        if (currentUser != null) {
            currentUser.apply {
                user = UserInfo(id, email, displayName, gender, status, Date.valueOf(dateJoined))
            }
        } else {
            user = null
        }

        _isInitialized.value = true
    }

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        _isInitialized.value = false

        uiScope.launch {
            initialize()
        }
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun login(username: String, password: String): Result<UserInfo> {
        // handle login
        val result = dataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: UserInfo) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}