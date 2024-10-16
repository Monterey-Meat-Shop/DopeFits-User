package com.example.dopefits.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dopefits.R
import com.example.dopefits.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.DataSnapshot

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

        Log.d("RegisterActivity", "Register button initialized: $registerButton")

        registerButton.setOnClickListener {
            Log.d("RegisterActivity", "Register button clicked")
            val fullName = registerFullName.text.toString()
            val username = registerUsername.text.toString()
            val email = registerEmail.text.toString()
            val phoneNumber = registerPhoneNumber.text.toString()
            val password = registerPassword.text.toString()
            if (validateInput(fullName, username, email, phoneNumber, password)) {
                registerUser(fullName, username, email, phoneNumber, password)
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInput(fullName: String, username: String, email: String, phoneNumber: String, password: String): Boolean {
        if (fullName.isEmpty()) {
            registerFullName.error = "Full name is required"
            registerFullName.requestFocus()
            return false
        }
        if (username.isEmpty()) {
            registerUsername.error = "Username is required"
            registerUsername.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            registerEmail.error = "Email is required"
            registerEmail.requestFocus()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerEmail.error = "Enter a valid email"
            registerEmail.requestFocus()
            return false
        }
        if (phoneNumber.isEmpty()) {
            registerPhoneNumber.error = "Phone number is required"
            registerPhoneNumber.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            registerPassword.error = "Password is required"
            registerPassword.requestFocus()
            return false
        }
        if (password.length < 6) {
            registerPassword.error = "Password should be at least 6 characters"
            registerPassword.requestFocus()
            return false
        }
        return true
    }

    private fun generateUniqueUserId(callback: (Int) -> Unit) {
        val userIdRef = database.child("metadata").child("unique_user_id")
        userIdRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var currentId = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentId + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (committed) {
                    callback(currentData?.getValue(Int::class.java) ?: 0)
                } else {
                    Toast.makeText(baseContext, "Failed to generate unique user ID.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun registerUser(fullName: String, username: String, email: String, phoneNumber: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    generateUniqueUserId { uniqueUserId ->
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val user = User(uniqueUserId, fullName, username, email, phoneNumber)
                            database.child("users").child(userId).setValue(user)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        Toast.makeText(baseContext, "Registration successful. Please log in.", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(baseContext, "Database update failed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "This email is already registered."
                        is FirebaseAuthWeakPasswordException -> "The password is too weak."
                        is FirebaseAuthInvalidCredentialsException -> "The email address is malformed."
                        else -> "Registration failed. Please try again."
                    }
                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}