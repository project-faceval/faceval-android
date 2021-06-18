package com.chardon.faceval.android.ui.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.data.LoginDataSource
import com.chardon.faceval.android.data.LoginRepository
import com.chardon.faceval.android.data.dao.UserDao

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory(private val userDao: UserDao,
                            private val application: Application) : ViewModelProvider.Factory {
    companion object Store {
        var viewModel: LoginViewModel? = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            synchronized(Store) {
                if (viewModel == null) {
                    viewModel = LoginViewModel(userDao, application)
                }

                return viewModel as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}