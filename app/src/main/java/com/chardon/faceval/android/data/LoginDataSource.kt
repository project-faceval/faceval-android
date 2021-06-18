package com.chardon.faceval.android.data

import com.chardon.faceval.android.data.dao.UserDao
import com.chardon.faceval.android.data.model.User
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.entity.UserInfo
import kotlinx.coroutines.*
import java.lang.Exception

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource(private val userDao: UserDao) {

    private var dataSourceJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + dataSourceJob)

    private val userClient = APISet.userClient

    suspend fun login(username: String, password: String): Result<UserInfo> {
        try {
            val deferredJob = userClient.loginAsync(username, password)

            val userInfo = deferredJob.await()

            userDao.insert(
                User(
                    userInfo.id, userInfo.email, password,
                    userInfo.dateAdded.toString(),
                    userInfo.displayName,
                    userInfo.gender,
                    userInfo.status, true
                )
            )

            return Result.Success(userInfo)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    fun logout() {
        userDao.deleteAll()
    }

    suspend fun isLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    private suspend fun getAllAsync(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAll()
        }
    }

    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            val userList = getAllAsync()
            if (userList.count() > 0) userList[0] else null
        }
    }
}

class LoginFailedException : Exception()