package com.budgettracker.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.data.entities.User
import com.budgettracker.databinding.ActivityLoginBinding
import com.budgettracker.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        // Auto-login if session exists
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (validateLoginInputs(username, password)) {
                performLogin(username, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (binding.tilConfirmPassword.visibility == View.GONE) {
                // Show register mode
                binding.tilConfirmPassword.visibility = View.VISIBLE
                binding.btnRegister.text = "Create Account"
                binding.tvRegisterLabel.text = "Already have an account?"
                binding.btnSwitchMode.text = "Login"
            } else {
                if (validateRegisterInputs(username, password, confirmPassword)) {
                    performRegister(username, password)
                }
            }
        }

        binding.btnSwitchMode.setOnClickListener {
            // Toggle back to login mode
            binding.tilConfirmPassword.visibility = View.GONE
            binding.etConfirmPassword.setText("")
            binding.btnRegister.text = "Register"
            binding.tvRegisterLabel.text = "Don't have an account?"
            binding.btnSwitchMode.text = "Register"
        }
    }

    private fun validateLoginInputs(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            return false
        }
        binding.tilUsername.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        binding.tilPassword.error = null

        return true
    }

    private fun validateRegisterInputs(username: String, password: String, confirm: String): Boolean {
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            return false
        }
        if (username.length < 3) {
            binding.tilUsername.error = "Username must be at least 3 characters"
            return false
        }
        binding.tilUsername.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return false
        }
        binding.tilPassword.error = null

        if (confirm != password) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            return false
        }
        binding.tilConfirmPassword.error = null

        android.util.Log.d("REGISTER", "Validation Passed!")
        return true
    }

    private fun performLogin(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            val user = db.userDao().login(username, password)
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                if (user != null) {
                    sessionManager.saveSession(user.id, user.username)
                    navigateToMain()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Invalid username or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun performRegister(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            try {
                val existing = db.userDao().getUserByUsername(username)
                android.util.Log.d("REGISTER", "Existing user: $existing")

                if (existing != null) {
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        binding.tilUsername.error = "Username already taken"
                    }
                    return@launch
                }

                val userId = db.userDao().insertUser(User(username = username, password = password))
                android.util.Log.d("REGISTER", "Insert result userId: $userId")

                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    if (userId > 0) {
                        sessionManager.saveSession(userId.toInt(), username)
                        Toast.makeText(this@LoginActivity, "Account created!", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("REGISTER", "Error: ${e.message}", e)
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
