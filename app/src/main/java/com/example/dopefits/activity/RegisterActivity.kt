package com.example.dopefits.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dopefits.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var registerButton: Button
    private lateinit var backButton: Button
    private lateinit var registerGivenName: EditText
    private lateinit var registerMiddleName: EditText
    private lateinit var registerSurname: EditText
    private lateinit var registerUsername: EditText
    private lateinit var registerEmail: EditText
    private lateinit var registerPhoneNumber: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerAddress: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        registerButton = findViewById(R.id.register_button)
        backButton = findViewById(R.id.back_button)
        registerGivenName = findViewById(R.id.register_given_name)
        registerMiddleName = findViewById(R.id.register_middle_name)
        registerSurname = findViewById(R.id.register_surname)
        registerUsername = findViewById(R.id.register_username)
        registerEmail = findViewById(R.id.register_email)
        registerPhoneNumber = findViewById(R.id.register_phone_number)
        registerPassword = findViewById(R.id.register_password)
        registerAddress = findViewById(R.id.register_address)

        registerPhoneNumber.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex("[0-9]+"))) {
                source
            } else {
                ""
            }
        })

        registerButton.setOnClickListener {
            val givenName = registerGivenName.text.toString()
            val middleName = registerMiddleName.text.toString()
            val surname = registerSurname.text.toString()
            val username = registerUsername.text.toString()
            val email = registerEmail.text.toString()
            val phoneNumber = registerPhoneNumber.text.toString()
            val password = registerPassword.text.toString()
            val address = registerAddress.text.toString()
            if (validateInput(givenName, middleName, surname, username, email, phoneNumber, password, address)) {
                registerUser(givenName, middleName, surname, username, email, phoneNumber, password, address)
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInput(givenName: String, middleName: String, surname: String, username: String, email: String, phoneNumber: String, password: String, address: String): Boolean {
        if (givenName.isEmpty()) {
            registerGivenName.error = "Given name is required"
            registerGivenName.requestFocus()
            return false
        }
        if (middleName.isEmpty()) {
            registerMiddleName.error = "Middle name is required"
            registerMiddleName.requestFocus()
            return false
        }
        if (surname.isEmpty()) {
            registerSurname.error = "Surname is required"
            registerSurname.requestFocus()
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
        if (address.isEmpty()) {
            registerAddress.error = "Address is required"
            registerAddress.requestFocus()
            return false
        }
        return true
    }

    private fun registerUser(givenName: String, middleName: String, surname: String, username: String, email: String, phoneNumber: String, password: String, address: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = User(givenName, middleName, surname, username, email, phoneNumber, address)
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

data class User(val givenName: String, val middleName: String, val surname: String, val username: String, val email: String, val phoneNumber: String, val address: String)