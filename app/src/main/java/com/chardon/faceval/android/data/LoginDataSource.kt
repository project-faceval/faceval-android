package com.chardon.faceval.android.data

import com.chardon.faceval.android.data.dao.UserDao
import com.chardon.faceval.android.data.model.User
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.model.UserInfo
import java.lang.Exception

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource(private val userDao: UserDao) {

    private val userClient = APISet.userClient

    fun login(username: String, password: String): Result<UserInfo> {
        try {
            val response = userClient.authenticate(username, password).execute()
            if (!response.isSuccessful || response.body()?.get("status") != "OK") {
                throw LoginFailedException()
            }

            val retrieveRes = userClient.getUser(username).execute()
            if (!retrieveRes.isSuccessful) {
                throw Exception("Cannot get user's information")
            }

            val userInfo = retrieveRes.body() ?: throw Exception("Cannot get user's information")

            userDao.insert(
                User(
                    userInfo.id, userInfo.email, password,
                    userInfo.dateAdded, userInfo.displayName, userInfo.gender,
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

    fun isLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    fun getCurrentUser(): User? {
        val userList = userDao.getAll()
        return if (userList.count() > 0) userList[0] else null
    }
}

class LoginFailedException : Exception()