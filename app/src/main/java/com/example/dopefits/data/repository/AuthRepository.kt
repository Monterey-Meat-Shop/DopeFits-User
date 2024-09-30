package com.example.dopefits.data.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.dopefits.data.models.User
import com.example.dopefits.ui.activities.LoginActivity
import com.example.dopefits.ui.activities.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun signIn(email: String, password: String, onComplete: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun registerUser(
        fullName: String,
        username: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ) {
        if (password != confirmPassword) {
            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = User(fullName, username, email, phoneNumber, password)
                        database.child("users").child(userId).setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Registration successful. Please log in.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(context, LoginActivity::class.java)
                                    context.startActivity(intent)
                                    if (context is RegisterActivity) {
                                        context.finish()
                                    }
                                } else {
                                    Log.e(
                                        "RegisterActivity",
                                        "Database update failed",
                                        dbTask.exception
                                    )
                                    Toast.makeText(
                                        context,
                                        "Database update failed.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "User ID is null.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "This email is already registered."
                        is FirebaseAuthWeakPasswordException -> "The password is too weak."
                        is FirebaseAuthInvalidCredentialsException -> "The email address is malformed."
                        else -> "Registration failed. Please try again."
                    }
                    Log.e("RegisterActivity", "Registration failed", task.exception)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}