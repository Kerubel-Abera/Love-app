package com.example.loveapp.ui.account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveapp.data.FirestoreRepository
import com.example.loveapp.data.Request
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import java.util.*

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

    private val _isTaken = MutableLiveData<Boolean>()
    val isTaken: LiveData<Boolean> = _isTaken


    private var requestsJob: Job
    private var isTakenJob: Job

    init {
        if (currentUser == null) {
            _navBackToLogin.value = true
        }

        requestsJob = viewModelScope.launch {
            Log.i("addLoverViewModel", "job started")
            repository.getAllRequests().cancellable().collect { requests ->
                _requests.value = requests
            }
        }
        isTakenJob = viewModelScope.launch {
            Log.i("addLoverViewModel", "second job started")
            repository.isTaken().cancellable().collect { isTaken ->
                Log.i("addLoverViewModel", "isTaken changed! $isTaken")
                _isTaken.value = isTaken
            }
        }
        _username.value = currentUser?.displayName
        _errorMessage.value = null
    }

    fun stopJob() {
        requestsJob.cancel()
        isTakenJob.cancel()
    }

    fun addLover(email: String, date: List<Int>) {
        val loverDate = Calendar.getInstance()
        loverDate.isLenient = false
        loverDate.set(date[2], date[1] - 1, date[0])
        val validDate = validateDate(loverDate)

        if (validDate != null) {
            _errorMessage.value = validDate
        } else if (email.isEmpty()) {
            _errorMessage.value = "Please fill in your lover's e-mail."
        } else {
            viewModelScope.launch {
                val success = repository.addLover(email, date)
                if (success == false) {
                    _errorMessage.value = "This e-mail does not exist."
                } else if (success == null) {
                    _errorMessage.value = "You cannot add yourself."
                }
            }
        }
    }

    fun declineRequest(request: Request) {
        viewModelScope.launch {
            repository.declineRequest(request)
        }
    }

    fun acceptRequest(request: Request) {
        viewModelScope.launch {
            repository.acceptRequest(request)
        }
    }

    private fun validateDate(loverDate: Calendar): String? {
        try {
            loverDate.get(Calendar.MONTH)
        } catch (_: Exception) {
            return "Invalid date."
        }

        return if (loverDate.after(Calendar.getInstance())) {
            "This date is in the future."
        } else {
            null
        }
    }

    fun onNavBackToLoginCompleted() {
        _navBackToLogin.value = null
    }

    fun completeErrorMessage() {
        _errorMessage.value = null
    }
}