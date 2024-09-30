package com.example.dopefits.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.dopefits.R
import com.example.dopefits.ui.viewmodels.AuthViewModel
import com.example.dopefits.ui.viewmodels.AuthViewModelFactory
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(this) }
    private lateinit var loginButton: Button
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var registerTextView: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.login_button)
        loginEmail = findViewById(R.id.login_email)
        loginPassword = findViewById(R.id.login_password)
        registerTextView = findViewById(R.id.register_text_view)

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()
            authViewModel.authenticateUser(email, password)
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        authViewModel.authResult.observe(this) { isSuccess ->
            if (isSuccess) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}