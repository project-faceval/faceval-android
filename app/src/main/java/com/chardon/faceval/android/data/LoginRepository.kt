package com.chardon.faceval.android.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chardon.faceval.android.data.model.User
import com.chardon.faceval.android.util.Action
import com.chardon.faceval.android.util.DateFormatUtil.parseDate
import com.chardon.faceval.android.util.LatePrepared
import com.chardon.faceval.entity.UserInfo
import kotlinx.coroutines.*
import java.util.*

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(private val dataSource: LoginDataSource,
                      private val whenReady: Action = Action {  }) : LatePrepared {

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

    private fun setDBUser(currentUser: User?) {
        user = currentUser?.let {
            UserInfo(
                it.id, it.email, it.displayName,
                it.gender, it.status,
                it.dateJoined?.parseDate() ?: Date()
            )
        }
    }

    suspend fun refreshAsync() {
        val currentUser = dataSource.getCurrentUser()
        setDBUser(currentUser)
        _isInitialized.value = true
    }

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        _isInitialized.value = false

        repoJob.invokeOnCompletion {
            whenReady.invoke()
        }

        uiScope.launch {
            refreshAsync()
            repoJob.complete()
        }
    }

    override fun ready(callback: Action) {
        if (repoJob.isCompleted) {
            callback.invoke()
        } else {
            repoJob.invokeOnCompletion {
                callback.invoke()
            }
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            dataSource.logout()
        }

        user = null
    }

    suspend fun login(username: String, password: String): Result<UserInfo> {
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