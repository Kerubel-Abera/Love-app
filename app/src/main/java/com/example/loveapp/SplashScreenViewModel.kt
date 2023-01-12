package com.example.loveapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveapp.data.FirestoreRepository
import kotlinx.coroutines.launch

class SplashScreenViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    private val _isTaken = MutableLiveData<Boolean>()
    val isTaken: LiveData<Boolean> = _isTaken

    init {
        viewModelScope.launch {
            _isTaken.value = repository.checkIsTakenOnce()
        }
    }
    fun onStart(): Boolean {
        return repository.onStart()
    }
}