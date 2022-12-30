package com.example.loveapp.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveapp.data.AuthRepository
import com.example.loveapp.data.Resource
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _loginData = MutableLiveData<Resource<FirebaseUser>?>(null)
    val loginData: LiveData<Resource<FirebaseUser>?> = _loginData

    private val _signupData = MutableLiveData<Resource<FirebaseUser>?>(null)
    val signupData: LiveData<Resource<FirebaseUser>?> = _signupData

    private val _passwordValidated = MutableLiveData<String?>()
    val passwordValidated: LiveData<String?> = _passwordValidated

    private val _navigate = MutableLiveData<String?>()
    val navigate: LiveData<String?> = _navigate

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val currentUser: FirebaseUser? = repository.currentUser

    init {
        _passwordValidated.value = null
        _errorMessage.value = null
        if(currentUser != null) {
            _loginData.value = Resource.Success(currentUser)
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _loginData.value = Resource.Loading
        val result = repository.login(email, password)
        _loginData.value = result
    }

    fun signup(name: String, email: String, password: String) = viewModelScope.launch {
        _signupData.value = Resource.Loading
        val result = repository.signup(name, email, password)
        _signupData.value = result
    }

    fun checkPassword(password: String, confirmPassword: String) {
        if(password != confirmPassword){
            _errorMessage.value = "Passwords do not match."
        } else {
            _passwordValidated.value = "validated"
        }
    }

    fun validatedPassword() {
        _passwordValidated.value = null
    }

    fun showErrorMessage(exception: Exception) {
        when(exception){
            is FirebaseAuthInvalidUserException ->
                _errorMessage.value = "This e-mail has no account."
            is java.lang.IllegalArgumentException ->
                _errorMessage.value = "One or multiple fields are empty."
            is FirebaseAuthInvalidCredentialsException ->
                _errorMessage.value = "Invalid e-mail address or password."
            else ->
                _errorMessage.value = exception.localizedMessage
        }
    }

    fun completeErrorMessage(){
        _errorMessage.value = null
    }

    fun startNavigate() {
        _navigate.value = "startNavigation"
    }

    fun finishNavigate(){
        _navigate.value = null
    }


    fun logout(){
        repository.logout()
        _loginData.value = null
        _signupData.value = null
    }
}