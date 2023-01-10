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
import java.time.LocalDate
import java.time.Month
import java.util.Calendar

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

    fun addLover(email: String, day: Int, month: Int, year: Int) {
        val loverDate = Calendar.getInstance()
        loverDate.isLenient = false
        loverDate.set(year, month - 1, day)
        val validDate = validateDate(loverDate)

        if(validDate != null){
            _errorMessage.value = validDate
        } else if (email.isEmpty()) {
            _errorMessage.value = "Please fill in your lover's e-mail."
        } else {
            viewModelScope.launch {
                val success = repository.addLover(email, day, month, year)
                if (success == false) {
                    _errorMessage.value = "This e-mail does not exist."
                }
            }
        }
    }

    private fun validateDate(loverDate: Calendar) : String? {
        try {
            loverDate.get(Calendar.MONTH)
        } catch (_: Exception) {
            return "Invalid date."
        }

        return if(loverDate.after(Calendar.getInstance())) {
            "This date is in the future."
        } else {
            null
        }
    }

    fun onNavBackToLoginCompleted(){
        _navBackToLogin.value = null
    }

    fun completeErrorMessage(){
        _errorMessage.value = null
    }
}