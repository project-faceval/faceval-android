package com.chardon.faceval.android.ui.profile

import android.annotation.SuppressLint
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
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.PhotoClient
import com.chardon.faceval.android.ui.login.LoginActivity
import com.chardon.faceval.android.ui.login.LoginViewModel
import com.chardon.faceval.android.ui.login.LoginViewModelFactory
import com.chardon.faceval.android.util.Base64Util.base64ToBitmap
import com.chardon.faceval.android.util.NotificationUtil.darkPurple
import com.chardon.faceval.entity.PhotoInfo
import com.chardon.faceval.entity.UserInfo
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.util.*
import kotlin.system.exitProcess

class ProfileFragment : Fragment() {
    companion object {
        private const val CALL_LOGIN = 0
        private const val CALL_SETTINGS = 1
    }

    private lateinit var binding: FragmentProfileBinding

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var profileViewModel: ProfileViewModel

    private val loaded: Boolean
        get() = profileViewModel.avatar.value != null

    private val avatarFetchJob = Job()
    private val avatarFetchScope = CoroutineScope(Dispatchers.Main + avatarFetchJob)

    private val photoClient: PhotoClient by lazy {
        APISet.photoClient
    }

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

            avatar.observe(viewLifecycleOwner) {
                binding.avatarView.setImageBitmap(it)
            }
        }

        binding.apply {
            loginButton.setOnClickListener {
                val intent = Intent(requireActivity().applicationContext, LoginActivity::class.java)

                startActivityForResult(intent, CALL_LOGIN)
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

            exitButton.setOnClickListener {
                exitProcess(0)
            }
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CALL_LOGIN -> {
                val extras = data?.extras ?: return

                if ((extras["loggedin"] as Boolean?) != true) {
                    return
                }

                profileViewModel.setUser(
                    UserInfo(
                        id = extras["username"] as String,
                        displayName = extras["display_name"] as String,
                        gender = extras["gender"] as Boolean?,
                        status = extras["status"] as String?,
                        dateAdded = Date(),
                        email = ""
                    )
                )

                refreshUI(reload = true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    @SuppressLint("ShowToast")
    private fun refreshUI(reload: Boolean = false) {
        if (!loaded || reload) {
            binding.avatarView.setImageResource(R.drawable.faceval)
        }

        loginViewModel.loginRepository.ready {
            val repo = loginViewModel.loginRepository

            binding.loginPrompt.isGone = repo.isLoggedIn

            if (repo.isLoggedIn && repo.user != null) {
                profileViewModel.setUser(repo.user!!)
            }

            avatarFetchScope.launch {
                val photos: List<PhotoInfo>

                try {
                     photos = photoClient.getPhotosAsync(
                        photoId = null, userName = repo.user!!.id, attachBase = false
                    ).await()
                } catch (e: Exception) {
//                    Snackbar.make(requireView(), "Cannot get photos", Snackbar.LENGTH_LONG)
//                        .darkPurple()
//                        .show()
                    return@launch
                }

                if (photos.isEmpty()) {
                    return@launch
                }

                val latestPhotoId = photos[0].id
                val photoInfo: PhotoInfo

                try {
                    photoInfo = photoClient.getPhotosAsync(
                        photoId = latestPhotoId, userName = repo.user!!.id, attachBase = true
                    ).await()[0]
                } catch (e: Exception) {
                    Snackbar.make(requireView(), "Cannot get photos", Snackbar.LENGTH_LONG)
                        .darkPurple()
                        .show()
                    return@launch
                }

                profileViewModel.setAvatar(photoInfo.base.base64ToBitmap())
            }
        }
    }
}