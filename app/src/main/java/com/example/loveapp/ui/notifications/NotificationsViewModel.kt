package com.example.loveapp.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.loveapp.data.FirestoreRepository

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text
    private val firestoreRepository = FirestoreRepository.getInstance()

    private val _logOut = MutableLiveData<Boolean>()
    val logOut: LiveData<Boolean> = _logOut

    init {
        _logOut.value = false
    }


    fun logOut(){
        _logOut.value = true
        firestoreRepository.logout()
    }

    fun finishLogOut(){
        _logOut.value = false
    }
}