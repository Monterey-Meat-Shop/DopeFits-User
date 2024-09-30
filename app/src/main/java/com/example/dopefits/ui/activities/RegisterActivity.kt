package com.example.dopefits.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.dopefits.R
import com.example.dopefits.data.repository.AuthRepository
import com.google.android.material.appbar.MaterialToolbar

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerFullName: EditText
    private lateinit var registerUsername: EditText
    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerConfirmPassword: EditText
    private lateinit var registerPhone: EditText
    private lateinit var registerButton: Button
    private val authRepository = AuthRepository(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerFullName = findViewById(R.id.register_full_name)
        registerUsername = findViewById(R.id.register_username)
        registerEmail = findViewById(R.id.register_email)
        registerPassword = findViewById(R.id.register_password)
        registerConfirmPassword = findViewById(R.id.register_confirm_password)
        registerPhone = findViewById(R.id.register_phone_number)
        registerButton = findViewById(R.id.register_button)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        registerButton.setOnClickListener {
            val name = registerFullName.text.toString()
            val username = registerUsername.text.toString()
            val email = registerEmail.text.toString()
            val password = registerPassword.text.toString()
            val confirmPassword = registerConfirmPassword.text.toString()
            val phone = registerPhone.text.toString()

            authRepository.registerUser(name, username, email, phone, password, confirmPassword)
        }
    }
}