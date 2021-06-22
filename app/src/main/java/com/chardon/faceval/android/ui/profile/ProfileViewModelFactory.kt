package com.chardon.faceval.android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProfileViewModelFactory: ViewModelProvider.Factory {
    companion object Store {
        @Volatile
        var viewModel: ProfileViewModel? = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            synchronized(Store) {
                if (viewModel == null) {
                    viewModel = ProfileViewModel()
                }

                return viewModel as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}