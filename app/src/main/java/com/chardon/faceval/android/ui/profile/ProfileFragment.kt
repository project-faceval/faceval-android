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
import androidx.room.Room
import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.LoginDataSource
import com.chardon.faceval.android.data.LoginRepository
import com.chardon.faceval.android.data.UserDatabase
import com.chardon.faceval.android.databinding.FragmentProfileBinding
import com.chardon.faceval.android.ui.login.LoginActivity
import com.chardon.faceval.android.ui.login.LoginViewModel

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

        val provider = ViewModelProvider(this)

        profileViewModel = provider.get(ProfileViewModel::class.java)
        loginViewModel = provider.get(LoginViewModel::class.java)

        profileViewModel.apply {
            displayName.observe(viewLifecycleOwner) {
                binding.displayNameLabel.text = it
            }

            userName.observe(viewLifecycleOwner) {
                binding.userIdLabel.text = it
            }
        }

        binding.loginButton.setOnClickListener {
            startActivity(Intent(requireActivity().applicationContext, LoginActivity::class.java))
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