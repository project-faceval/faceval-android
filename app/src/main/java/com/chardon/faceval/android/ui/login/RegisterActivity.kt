package com.chardon.faceval.android.ui.login

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.databinding.ActivityRegisterBinding
import com.chardon.faceval.android.util.Extensions.afterTextChanged
import com.chardon.faceval.entity.UserInfo
import com.chardon.faceval.entity.UserInfoUpload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val registerJob = Job()

    private val registerScope = CoroutineScope(registerJob)

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var viewModel: RegisterViewModel

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            intent.putExtra("canceled", true)
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.registerViewModel = viewModel
        binding.lifecycleOwner = this
        binding.registerActionButton.isEnabled = false

        intent.putExtra("canceled", false)

        viewModel.registerForm.observe(this@RegisterActivity) {
            val state = it ?: return@observe

            binding.apply {
                registerActionButton.isEnabled = state.isDataValid

                if (state.userNameError != null) {
                    userIdEntry.error = getString(state.userNameError)
                }

                if (state.conflictUsername != null) {
                    userIdEntry.error = getString(state.conflictUsername!!)
                }

                if (state.passwordError != null) {
                    passwordEntry.error = getString(state.passwordError)
                }

                if (state.displayNameError != null) {
                    displayNameEntry.error = getString(state.displayNameError)
                }

                if (state.emailError != null) {
                    emailEntry.error = getString(state.emailError)
                }

                if (state.confirmPasswordError != null) {
                    passwordConfirmEntry.error = getString(state.confirmPasswordError)
                }
            }
        }

        viewModel.registerResult.observe(this@RegisterActivity) {
            val registerResult = it ?: return@observe

            if (registerResult.error != null) {
                showRegisterFailed(registerResult.error)
            } else if (registerResult.success != null) {
                registerResult.success.apply {
                    updateUiWithUser(id, password)
                }

                intent.putExtra("canceled", false)
                setResult(Activity.RESULT_OK)
                viewModel.reset()
                finish()
            }
        }

        binding.apply {
            val entries = setOf(
                userIdEntry, emailEntry, displayNameEntry, passwordEntry, passwordConfirmEntry
            )

            entries.forEach {
                it.afterTextChanged {
                    registerViewModel!!.registerDataChanged(
                        userName = userIdEntry.text.toString(),
                        password = passwordEntry.text.toString(),
                        email = emailEntry.text.toString(),
                        displayName = displayNameEntry.text.toString(),
                        confirmPassword = passwordConfirmEntry.text.toString(),
                    )
                }
            }
        }

        binding.cancelRegisterButton.setOnClickListener {
            intent.putExtra("canceled", true)
            setResult(Activity.RESULT_CANCELED)
            viewModel.reset()
            finish()
        }

        binding.registerActionButton.setOnClickListener {
            binding.apply {
                viewModel.register(
                    UserInfoUpload(
                        id = userIdEntry.text.toString(),
                        password = passwordEntry.text.toString(),
                        email = emailEntry.text.toString(),
                        displayName = displayNameEntry.text.toString(),
                        status = null,
                        gender = null,
                    )
                )
            }
        }
    }

    private fun updateUiWithUser(username: String, password: String) {
        Toast.makeText(
            applicationContext,
            "Register succeed",
            Toast.LENGTH_LONG
        ).show()

        intent.putExtra("username", username)
        intent.putExtra("password", password)
    }

    private fun showRegisterFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}