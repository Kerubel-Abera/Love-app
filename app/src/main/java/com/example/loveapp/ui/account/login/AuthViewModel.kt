package com.example.loveapp.ui.account.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveapp.data.AuthRepository
import com.example.loveapp.data.FirestoreRepository
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

    private val _isTaken = MutableLiveData<Boolean?>(null)
    val isTaken: LiveData<Boolean?> = _isTaken

    private val currentUser: FirebaseUser? = repository.currentUser
    private val firestoreRepository = FirestoreRepository.getInstance()


    init {
        //initialize the data to null
        _passwordValidated.value = null
        _errorMessage.value = null
        //checks if user is already logged in
        if (currentUser != null) {
            _loginData.value = Resource.Success(currentUser)
        }
    }

    /**
     * This function logs the user in with the help of the FirebaseAuth API
     * @param email     The user his e-mail
     * @param password  The user his password
     *
     * @return the login data that of the user
     */
    fun login(email: String, password: String) = viewModelScope.launch {
        _loginData.value = Resource.Loading
        val result = repository.login(email, password)
        _loginData.value = result
    }


    /**
     * This function signs the user up with the help of the FirebaseAuth API
     * @param name      The user his username
     * @param email     The user his e-mail
     * @param password  The user his password
     *
     * @return the login data that of the user
     */
    fun signup(name: String, email: String, password: String) = viewModelScope.launch {
        _signupData.value = Resource.Loading
        val result = repository.signup(name, email, password)
        _signupData.value = result
    }

    fun addNewUser() {
        viewModelScope.launch {
            try {
                firestoreRepository.createUser()
            } catch (e: Exception) {
                Log.i("AuthViewModel", e.printStackTrace().toString())
            }
        }
    }

    fun checkTakenUser() {
        var isTaken = false
        viewModelScope.launch {
            try {
                isTaken = firestoreRepository.checkIsTakenOnce()
                _isTaken.value = isTaken
            } catch (e: Exception) {
                Log.i("AuthViewModel", e.printStackTrace().toString())
            }
        }
    }

    fun finishTakenUserCheck() {
        _isTaken.value = null
    }

    fun checkPassword(password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match."
        } else {
            _passwordValidated.value = "validated"
        }
    }

    fun validatedPassword() {
        _passwordValidated.value = null
    }

    /**
     * This function takes an exception and specifies it back
     * @param exception     The exception from the auth API
     *
     * @return the specified exception in the errorMessage variable
     */
    fun showErrorMessage(exception: Exception) {
        when (exception) {
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

    fun completeErrorMessage() {
        _errorMessage.value = null
    }

    fun startNavigate() {
        _navigate.value = "startNavigation"
    }

    fun finishNavigate() {
        _navigate.value = null
    }


    fun logout() {
        repository.logout()
        _loginData.value = null
        _signupData.value = null
    }
}