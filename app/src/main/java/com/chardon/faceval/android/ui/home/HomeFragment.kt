package com.chardon.faceval.android.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.camera.core.ImageCapture
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
import com.chardon.faceval.android.ui.scoring.ScoringActivity
import com.chardon.faceval.android.ui.shutter.ShutterActivity

class HomeFragment : Fragment() {
    companion object {
        private const val PICK_IMAGE = 0
        private const val CALL_SCORE = 1
        private const val CALL_CAMERA = 2
    }

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
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), PICK_IMAGE)
    }

    private fun callScoring(image: Bitmap) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CALL_CAMERA -> {

            }
            PICK_IMAGE -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }


            }
            CALL_SCORE -> {

            }
            else -> {}
        }
    }
//
//    override fun onCreateContextMenu(
//        menu: ContextMenu,
//        v: View,
//        menuInfo: ContextMenu.ContextMenuInfo?
//    ) {
//        super.onCreateContextMenu(menu, v, menuInfo)
//        val inflater: MenuInflater = requireActivity().menuInflater
//        inflater.inflate(R.menu.image_source_chooser_menu, menu)
//    }
//
//    override fun onContextItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.takePictureMenuItem -> {
//                // TODO: Call camerax
//                true
//            }
//            R.id.pickGalleryMenuItem -> {
//                // TODO: Call image picker
//                true
//            }
//            else -> super.onContextItemSelected(item)
//        }
//    }

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