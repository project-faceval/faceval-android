package com.chardon.faceval.android.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentContainerView
import com.chardon.faceval.android.databinding.ActivityLoginBinding

import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.UserDatabase
import com.chardon.faceval.android.data.dao.UserDao
import com.chardon.faceval.android.util.MiscExtensions.afterTextChanged
import java.util.*

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val CALL_REGISTER = 0
    }

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    private lateinit var userDao: UserDao

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.putExtra("loggedin", false)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val registerButton = binding.registerButton

        userDao = UserDatabase.getInstance(application).userDao

        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory(userDao, application))
            .get(LoginViewModel::class.java)

        binding.loginViewModel = loginViewModel
        binding.lifecycleOwner = this

        loginViewModel.loginFormState.observe(this@LoginActivity) {
            val loginState = it ?: return@observe

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        }

        loginViewModel.loginResult.observe(this@LoginActivity) {
            val loginResult = it ?: return@observe

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            } else if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }

            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        }

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> login(loading, username, password)
                }
                false
            }
        }

        login.setOnClickListener {
            login(loading, username, password)
        }

        binding.cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        registerButton.setOnClickListener {
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            startActivityForResult(intent, CALL_REGISTER)
        }
    }

    private fun login(
        loading: FragmentContainerView,
        username: EditText,
        password: EditText
    ) {
        loading.visibility = View.VISIBLE
        loginViewModel.login(username.text.toString(), password.text.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CALL_REGISTER -> {
                val extras = data?.extras ?: return

                if (extras.get("canceled") != true) {
                    binding.username.setText(
                        extras.get("username") as String,
                        TextView.BufferType.EDITABLE
                    )

                    binding.password.setText(
                        extras.get("password") as String,
                        TextView.BufferType.EDITABLE
                    )

//                binding.login.performClick()
                }
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()

        intent.putExtra("username", model.userId)
        intent.putExtra("display_name", model.displayName)
        intent.putExtra("gender", model.gender)
        intent.putExtra("status", model.status)
        intent.putExtra("loggedin", true)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}