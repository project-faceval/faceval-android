package com.chardon.faceval.android.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.PhotoClient
import com.chardon.faceval.android.ui.login.LoginViewModel
import com.chardon.faceval.android.ui.login.LoginViewModelFactory
import com.chardon.faceval.android.ui.scoring.ScoringActivity
import com.chardon.faceval.android.ui.shutter.ShutterActivity
import com.chardon.faceval.android.util.BitmapUtil.save
import com.chardon.faceval.android.util.BitmapUtil.toBitmap
import com.chardon.faceval.android.util.FileNames
import com.chardon.faceval.android.util.NotificationUtil.darkPurple
import com.chardon.faceval.entity.PhotoInfo
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class HomeFragment : Fragment() {
    companion object {
        private const val PICK_IMAGE = 0
        private const val CALL_SCORE = 1
        private const val CALL_CAMERA = 2
    }

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var binding: FragmentHomeBinding

    private val photoClient: PhotoClient by lazy {
        APISet.photoClient
    }

    private val bestScoreUpdateJob = Job()
    private val bestScoreUpdateScope = CoroutineScope(Dispatchers.Main + bestScoreUpdateJob)

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

        binding.getStartedButton.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.image_source_select)
                .setItems(R.array.image_methods) { _, which ->
                    when (which) {
                        0 -> callCamera()
                        1 -> pickLocalImage()
                        else -> {}
                    }
                }
                .show()
        }

        return binding.root
    }

    private fun callCamera() {
        val intent = Intent(requireActivity().applicationContext, ShutterActivity::class.java)
        startActivityForResult(intent, CALL_CAMERA)
    }

    private fun pickLocalImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        val chooser = Intent.createChooser(intent, getString(R.string.select_photo))
        startActivityForResult(chooser, PICK_IMAGE)
    }

    private fun callScoring(path: String) {
        val intent = Intent(requireActivity().applicationContext, ScoringActivity::class.java)
        intent.putExtra("image_path", path)
        startActivityForResult(intent, CALL_SCORE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CALL_CAMERA -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }

                val image = data?.extras?.getString("image")

                if (image == null) {
                    imageLoadFailedNotification()
                    return
                }

                callScoring(image)
            }
            PICK_IMAGE -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }

                if (data?.data == null) {
                    imageLoadFailedNotification()
                    return
                }

                val bitmap: Bitmap?

                context?.contentResolver?.openInputStream(data.data!!).use {
                    bitmap = it?.toBitmap()
                }

                if (bitmap == null) {
                    imageLoadFailedNotification()
                    return
                }

//                requireActivity().openFileOutput(FileNames.RAW_IMG, Context.MODE_PRIVATE).use {
//                    bitmap.save(it)
//                }
                callScoring(FileNames.RAW_IMG)
            }
            CALL_SCORE -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }

                requireActivity().intent.putExtra("navigate_add", true)
                view?.findNavController()?.navigate(R.id.action_navigation_home_to_navigation_dashboard)
            }
            else -> {}
        }
    }

    @SuppressLint("ShowToast")
    private fun imageLoadFailedNotification() {
        Snackbar.make(requireView(), R.string.image_load_failed, Snackbar.LENGTH_LONG)
            .darkPurple()
            .show()
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

                bestScoreUpdateScope.launch {
                    val photos = photoClient.getPhotosAsync(
                        photoId = null, userName = repo.user!!.id, attachBase = false).await()

                    val maxScoreOptional = photos.stream().max(Comparator<PhotoInfo> { o1, o2 ->
                        if (o1 == null && o2 == null) {
                            return@Comparator 0
                        }

                        if (o1 == null) {
                            return@Comparator -1
                        }

                        if (o2 == null) {
                            return@Comparator 1
                        }

                        o1.score.compareTo(o2.score)
                    })

                    if (maxScoreOptional.isPresent) {
                        binding.bestScoreLabel.text = getString(R.string.best_score_prompt)
                        binding.scoreShowLabel.text = String.format("%.1f", maxScoreOptional.get().score)
                    } else {
                        binding.bestScoreLabel.text = getString(R.string.take_photo_prompt)
                        binding.scoreShowLabel.text = ""
                    }
                }
            } else {
                binding.aChicken.text = getString(R.string.egg_emoji)
                binding.signinPrompt.text = getString(R.string.signin_prompt)
            }
        }
    }

    fun activateShutter(imageSourceOption: String) {
        val intent = Intent(requireActivity().applicationContext, ScoringActivity::class.java)

        intent.putExtra("image_source", imageSourceOption)

        startActivityForResult(intent, 0)

        val extras = intent.extras ?: return

        if (extras.getBoolean("shot")) {
            view?.findNavController()?.navigate(R.id.action_navigation_home_to_navigation_dashboard)
        }
    }
}