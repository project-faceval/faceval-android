package com.chardon.faceval.android.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.UserDatabase
import com.chardon.faceval.android.databinding.FragmentProfileBinding
import com.chardon.faceval.android.ui.login.LoginActivity
import com.chardon.faceval.android.ui.login.LoginViewModel
import com.chardon.faceval.android.ui.login.LoginViewModelFactory
import com.chardon.faceval.entity.UserInfo
import kotlinx.coroutines.*
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile, container, false)

        val application = requireActivity().application

        val userDao = UserDatabase.getInstance(application).userDao

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

                startActivityForResult(intent, 0)

                val extras = intent.extras ?: return@setOnClickListener

                if ((extras["loggedin"] as Boolean?) != true) {
                    return@setOnClickListener
                }

                profileViewModel?.setUser(
                    UserInfo(
                        id = extras["username"] as String,
                        displayName = extras["display_name"] as String,
                        gender = extras["gender"] as Boolean,
                        status = extras["status"] as String,
                        dateAdded = Date(),
                        email = ""
                    )
                )

                val localJob = Job()
                val localScope = CoroutineScope(Dispatchers.Main + localJob)

                localJob.invokeOnCompletion {
                    refreshUI()
                }

                localScope.launch {
                    loginViewModel?.loginRepository?.refreshAsync()
                    localJob.complete()
                }
            }

            logoutButton.setOnClickListener {
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.logout_confirm_title)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        loginViewModel?.logout {
                            loginPrompt.isGone = false
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .show()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        loginViewModel.loginRepository.ready {
            val repo = loginViewModel.loginRepository

            binding.loginPrompt.isGone = repo.isLoggedIn

            if (repo.isLoggedIn && repo.user != null) {
                profileViewModel.setUser(repo.user!!)
            }
        }
    }
}