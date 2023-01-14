package com.example.loveapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveapp.data.FirestoreRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class SplashScreenViewModel : ViewModel() {
    private val repository = FirestoreRepository.getInstance()
    private val currentUser = repository.getCurrentUser()

    private val _isTaken = MutableLiveData<Boolean>()
    val isTaken: LiveData<Boolean> = _isTaken

    init {
        if (currentUser != null){
            viewModelScope.launch {
                _isTaken.value = repository.checkIsTakenOnce()
            }
        } else {
            _isTaken.value = false
        }
    }
}