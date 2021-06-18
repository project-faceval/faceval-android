package com.chardon.faceval.android.ui.profile

import android.media.Image
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.PhotoClient
import com.chardon.faceval.entity.UserInfo

class ProfileViewModel : ViewModel() {

    private val _displayName = MutableLiveData<String>()
    val displayName: LiveData<String> = _displayName

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _gender = MutableLiveData<Boolean?>()
    val gender: LiveData<Boolean?> = _gender

    fun setUser(userInfo: UserInfo) {
        _displayName.value = userInfo.displayName
        _userName.value = userInfo.id
        _gender.value = userInfo.gender
    }
}