package com.chardon.faceval.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.UserDatabase
import com.chardon.faceval.android.databinding.FragmentHomeBinding
import com.chardon.faceval.android.ui.login.LoginViewModel
import com.chardon.faceval.android.ui.login.LoginViewModelFactory

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_home, container, false
        )

        binding.statsFrame.visibility = View.GONE

        val application = requireActivity().application

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        loginViewModel = ViewModelProvider(this,
            LoginViewModelFactory(
                UserDatabase.getInstance(application).userDao, application
            )).get(LoginViewModel::class.java)

        binding.homeViewModel = homeViewModel
        binding.loginViewModel = loginViewModel
        binding.lifecycleOwner = this

        binding.loginButtonHome.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_navigation_home_to_navigation_profile)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        loginViewModel.loginRepository.ready {
            val repo = loginViewModel.loginRepository

            binding.statsFrame.isGone = !repo.isLoggedIn
            binding.loginButtonHome.isGone = repo.isLoggedIn
            binding.getStartedButton.isEnabled = repo.isLoggedIn

            if (repo.isLoggedIn && repo.user != null) {
                binding.aChicken.text = getString(R.string.chicken_emoji)
                binding.signinPrompt.text = getString(R.string.signedin_prompt)
            } else {
                binding.aChicken.text = getString(R.string.egg_emoji)
                binding.signinPrompt.text = getString(R.string.signin_prompt)
            }
        }
    }
}