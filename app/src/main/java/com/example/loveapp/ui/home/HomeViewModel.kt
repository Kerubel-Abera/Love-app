package com.example.loveapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveapp.data.Couple
import com.example.loveapp.data.FirestoreRepository
import kotlinx.coroutines.launch
import java.lang.Math.round
import java.util.*
import kotlin.math.roundToInt

class HomeViewModel : ViewModel() {

    private val _passedDays = MutableLiveData<Int>()
    val passedDays: LiveData<Int> = _passedDays

    private val _coupleData = MutableLiveData<Couple>()
    val coupleData: LiveData<Couple> = _coupleData

    private val repository = FirestoreRepository.getInstance()

    init{
        getCoupleData()
    }

    private fun getCoupleData() {
        viewModelScope.launch {
            _coupleData.value = repository.getCoupleData()
        }
    }
    fun getAmountOfDays(date: List<Int>) {
        val currentDate = Calendar.getInstance()
        val savedDate = Calendar.getInstance()
        savedDate.set(date[2], date[1]-1, date[0])
        val diff = currentDate.timeInMillis - savedDate.timeInMillis
        val diffDays = (diff / (24 * 60 * 60 * 1000)).toDouble().roundToInt()
        _passedDays.value = diffDays
    }
}