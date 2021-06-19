package com.chardon.faceval.android.util

import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.userNameCheckClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FieldChecker {

    private val userNamePattern = Regex("^[^\\s]{5,20}$")

    private val passwordPattern = Regex("^.{5,20}$")

    private val displayNamePattern = Regex("^[^\\s].{3,118}[^\\s]$")

    private val emailPattern = Regex("^[^\\s]+@[^\\s]+\\.[^\\s]+$")

    private val userClient = APISet.userNameCheckClient

    private val taboos = setOf(
        "admin",
        "fuck",
        "shit",
        "dick",
        "damn",
        "negro",
        "negress",
        "president",
        "chairman",
        "patriarchy",
        "funny mud pee",
        "funnymudpee",
    )

    fun username(username: String): Boolean {
        for (item in taboos) {
            if (username.lowercase().contains(item)) {
                return false
            }
        }

//        return Pattern.matches("^[^\\s]+$", username)
        return username.matches(userNamePattern)
    }

    suspend fun usernameAsync(username: String): Boolean {
        return withContext(Dispatchers.IO) {
            userClient.checkValidity(username).await().trim() != "INVALID"
        }
    }

    fun password(password: String): Boolean {
        return password.matches(passwordPattern)
    }

    fun displayName(displayName: String): Boolean {
        for (item in taboos) {
            if (displayName.lowercase().contains(item)) {
                return false
            }
        }

        return displayName.matches(displayNamePattern)
    }

    fun email(email: String): Boolean {
        return email.matches(emailPattern)
    }

    suspend fun usernameNotConflict(username: String): Boolean {
        return withContext(Dispatchers.IO) {
            userClient.checkValidity(username).await().trim() != "EXISTS"
        }
    }
}