package com.chardon.faceval.android.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.room.Room
import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.LoginDataSource
import com.chardon.faceval.android.data.LoginRepository
import com.chardon.faceval.android.data.UserDatabase
import com.chardon.faceval.android.data.dao.UserDao
import com.chardon.faceval.android.databinding.FragmentProfileBinding
import com.chardon.faceval.android.ui.login.LoginActivity
import com.chardon.faceval.android.ui.login.LoginViewModel
import com.chardon.faceval.android.ui.login.LoginViewModelFactory
import com.chardon.faceval.entity.UserInfo
import java.sql.Date

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var userDao: UserDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile, container, false)

        val application = requireActivity().application

        userDao = UserDatabase.getInstance(application).userDao

        val provider = ViewModelProvider(this)
        val loginViewModelProvider = ViewModelProvider(
            this,
            LoginViewModelFactory(userDao, application)
        )

        profileViewModel = provider.get(ProfileViewModel::class.java)
        loginViewModel = loginViewModelProvider.get(LoginViewModel::class.java)

        binding.profileViewModel = profileViewModel
        binding.loginViewModel = loginViewModel
        binding.lifecycleOwner = this

        profileViewModel.apply {
            displayName.observe(viewLifecycleOwner) {
                binding.displayNameLabel.text = it
            }

            userName.observe(viewLifecycleOwner) {
                binding.userIdLabel.text = it
            }
        }

        binding.apply {
            loginButton.setOnClickListener {
                val intent = Intent(requireActivity().applicationContext, LoginActivity::class.java)

                startActivity(intent)

                val extras = intent.extras ?: return@setOnClickListener

                if (!(extras["loggedin"] as Boolean)) {
                    return@setOnClickListener
                }

                profileViewModel?.setUser(
                    UserInfo(
                        id = extras["username"] as String,
                        displayName = extras["display_name"] as String,
                        gender = extras["gender"] as Boolean,
                        status = extras["status"] as String,
                        dateAdded = Date.valueOf(extras["date_added"] as String),
                        email = ""
                    )
                )
            }

            logoutButton.setOnClickListener {
                loginViewModel?.loginRepository?.logout()
                loginPrompt.isGone = false
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val repo = loginViewModel.loginRepository

        binding.loginPrompt.isGone = repo.isLoggedIn

        if (repo.isLoggedIn && repo.user != null) {
            profileViewModel.setUser(repo.user!!)
        }
    }
}