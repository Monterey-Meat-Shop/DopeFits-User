package com.example.dopefits.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dopefits.data.repository.AuthRepository

class AuthViewModel(context: Context) : ViewModel() {
    private val authRepository = AuthRepository(context)

    private val _authResult = MutableLiveData<Boolean>()
    val authResult: LiveData<Boolean> get() = _authResult

    fun authenticateUser(email: String, password: String) {
        authRepository.signIn(email, password) { isSuccess ->
            _authResult.value = isSuccess
        }
    }
}