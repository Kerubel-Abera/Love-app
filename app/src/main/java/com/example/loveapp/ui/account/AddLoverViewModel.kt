package com.example.loveapp.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveapp.data.FirestoreRepository
import com.example.loveapp.data.Request
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AddLoverViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val currentUser = repository.getCurrentUser()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _navBackToLogin = MutableLiveData<Boolean?>()
    val navBackToLogin: LiveData<Boolean?> = _navBackToLogin

    private val _requests = MutableLiveData<List<Request>?>()
    val requests: LiveData<List<Request>?> = _requests

    private var job: Job

    init {
        if (currentUser == null){
            _navBackToLogin.value = true
        }

        job = viewModelScope.launch {
            repository.getAllRequests().cancellable().collect{ requests ->
                _requests.value = requests
            }
        }
        _username.value = currentUser?.displayName
        _errorMessage.value = null
    }

    fun stopRequestListListener(){
        job.cancel()
    }

    fun addLover(email: String) {
        if (email.isEmpty()) {
            _errorMessage.value = "Please fill in your lover's e-mail."
        } else {
            viewModelScope.launch {
                val success = repository.addLover(email)
                if (success == false) {
                    _errorMessage.value = "This e-mail does not exist."
                }
            }
        }
    }

    fun onNavBackToLoginCompleted(){
        _navBackToLogin.value = null
    }

    fun completeErrorMessage(){
        _errorMessage.value = null
    }
}