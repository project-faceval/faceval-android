package com.chardon.faceval.android.ui.login

import androidx.lifecycle.ViewModel
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.UserClient
import com.chardon.faceval.entity.UserInfo
import com.chardon.faceval.entity.UserInfoUpload

class RegisterViewModel : ViewModel() {

    private val userClient: UserClient by lazy {
        APISet.userClient
    }

    suspend fun register(userInfoUpload: UserInfoUpload): UserInfo? {
        val deferredJob = userClient.createUserAsync(userInfoUpload)

        return try {
            deferredJob.await()
        } catch (e: Exception) {
            null
        }
    }
}