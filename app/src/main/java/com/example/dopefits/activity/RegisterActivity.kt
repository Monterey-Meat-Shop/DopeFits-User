package com.example.dopefits.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dopefits.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var registerButton: Button
    private lateinit var backButton: Button
    private lateinit var registerFullName: EditText
    private lateinit var registerUsername: EditText
    private lateinit var registerEmail: EditText
    private lateinit var registerPhoneNumber: EditText
    private lateinit var registerPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        registerButton = findViewById(R.id.register_button)
        backButton = findViewById(R.id.back_button)
        registerFullName = findViewById(R.id.register_full_name)
        registerUsername = findViewById(R.id.register_username)
        registerEmail = findViewById(R.id.register_email)
        registerPhoneNumber = findViewById(R.id.register_phone_number)
        registerPassword = findViewById(R.id.register_password)

        registerButton.setOnClickListener {
            val fullName = registerFullName.text.toString()
            val username = registerUsername.text.toString()
            val email = registerEmail.text.toString()
            val phoneNumber = registerPhoneNumber.text.toString()
            val password = registerPassword.text.toString()
            registerUser(fullName, username, email, phoneNumber, password)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser(fullName: String, username: String, email: String, phoneNumber: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = User(fullName, username, email, phoneNumber)
                        database.child("users").child(userId).setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(baseContext, "Database update failed.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(baseContext, "Registration failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

data class User(val fullName: String, val username: String, val email: String, val phoneNumber: String)