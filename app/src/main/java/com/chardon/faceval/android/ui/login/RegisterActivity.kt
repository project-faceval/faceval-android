package com.chardon.faceval.android.ui.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.databinding.ActivityRegisterBinding
import com.chardon.faceval.entity.UserInfoUpload

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.cancelRegisterButton.setOnClickListener {
            intent.putExtra("canceled", true)
            finish()
        }

        binding.registerActionButton.setOnClickListener {
            val userName = binding.userIdEntry.text.toString()
            val email = binding.emailEntry.text.toString()
            val displayName = binding.displayNameEntry.text.toString()
            val password = binding.passwordEntry.text.toString()

            if (userName.isBlank()) {
                Toast.makeText(
                    applicationContext, getString(R.string.no_username), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (email.isBlank()) {
                Toast.makeText(
                    applicationContext, getString(R.string.no_email), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (displayName.isBlank()) {
                Toast.makeText(
                    applicationContext, getString(R.string.no_display_name), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(
                    applicationContext, getString(R.string.no_password), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (password != binding.passwordConfirmEntry.text.toString()) {
                Toast.makeText(
                    applicationContext, getString(R.string.password_not_same), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val userInfo = viewModel.register(UserInfoUpload(
                id = userName,
                password = password,
                email = email,
                displayName = displayName,
                status = null,
                gender = null,
            ))

            if (userInfo == null) {
                Toast.makeText(
                    applicationContext, getString(R.string.register_failed), Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            intent.putExtra("canceled", false)
            intent.putExtra("username", userName)
            intent.putExtra("password", password)
            finish()
        }
    }
}