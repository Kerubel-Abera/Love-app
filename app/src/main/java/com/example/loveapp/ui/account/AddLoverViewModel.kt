package com.example.loveapp.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveapp.data.FirestoreRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class AddLoverViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    val currentUser = repository.getCurrentUser()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    init {
        _username.value = currentUser?.displayName
    }

    fun addLover(email: String) {
        viewModelScope.launch {
            repository.addLover(email)
        }
    }
}