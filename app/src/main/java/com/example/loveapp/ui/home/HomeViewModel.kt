package com.example.loveapp.ui.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveapp.data.Couple
import com.example.loveapp.data.FirestoreRepository
import com.example.loveapp.data.Milestone
import com.example.loveapp.data.Request
import kotlinx.coroutines.launch
import java.lang.Math.round
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class HomeViewModel : ViewModel() {

    private val _passedDays = MutableLiveData<Int>()
    val passedDays: LiveData<Int> = _passedDays

    private val _coupleData = MutableLiveData<Couple>()
    val coupleData: LiveData<Couple> = _coupleData

    private val _firstUserIcon = MutableLiveData<Uri>()
    val firstUserIcon: LiveData<Uri> = _firstUserIcon

    private val _secondUserIcon = MutableLiveData<Uri>()
    val secondUserIcon: LiveData<Uri> = _secondUserIcon

    private val _milestones = MutableLiveData<List<Milestone>?>()
    val milestones: LiveData<List<Milestone>?> = _milestones

    private val repository = FirestoreRepository.getInstance()

    init{
        getCoupleData()
        updateUserIcons()
    }

    fun getMilestones() {
        val date = _coupleData.value?.date
        if(date != null){
            val milestones = mutableListOf<Milestone>()
            val currentDate = Calendar.getInstance()
            var anniversaryCounter = 0
            var anniversaryDate = Calendar.getInstance()
            anniversaryDate.set(date[2], date[1]-1, date[0])
            anniversaryDate.add(Calendar.MONTH, 1)

            while (anniversaryDate <= currentDate) {
                anniversaryCounter++
                val anniversaryUnits = if (anniversaryCounter % 12 == 0) "year" else "month"
                val anniversaryPeriod = if (anniversaryCounter % 12 == 0) anniversaryCounter / 12 else anniversaryCounter
                if(anniversaryDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                    anniversaryDate.get(Calendar.DATE) == currentDate.get(Calendar.DATE)){
                    milestones.add(0, Milestone(
                        "$anniversaryPeriod $anniversaryUnits anniversary is today!",
                        "Today!"
                    ))
                } else if(anniversaryCounter == 12) {
                    anniversaryDate.add(Calendar.MONTH, 1)
                    milestones.add(Milestone(
                        "$anniversaryPeriod $anniversaryUnits anniversary",
                        "${anniversaryDate.get(Calendar.DATE)} / ${anniversaryDate.get(Calendar.MONTH)+1} / ${anniversaryDate.get(Calendar.YEAR)}"
                    ))
                }else {
                    milestones.add(Milestone(
                        "$anniversaryPeriod $anniversaryUnits anniversary",
                        "${anniversaryDate.get(Calendar.DATE)} / ${anniversaryDate.get(Calendar.MONTH)+1} / ${anniversaryDate.get(Calendar.YEAR)}"
                    ))
                    anniversaryDate.add(Calendar.MONTH, 1)
                }
            }
            val nextAnniversary = Calendar.getInstance()
            nextAnniversary.set(date[2], date[1]-1, date[0])
            nextAnniversary.add(Calendar.MONTH, anniversaryCounter+1)
            val nextAnniversaryUnits = if (anniversaryCounter % 12 == 0) "year" else "month"
            val nextAnniversaryPeriod = if (anniversaryCounter % 12 == 0) anniversaryCounter / 12 + 1 else anniversaryCounter + 1
            val diff = nextAnniversary.timeInMillis - currentDate.timeInMillis
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            milestones.add(Milestone(
                "$nextAnniversaryPeriod $nextAnniversaryUnits anniversary in",
                "$days days"
            ))
            _milestones.value = milestones.reversed()
        }
    }

private fun getCoupleData() {
        viewModelScope.launch {
            _coupleData.value = repository.getCoupleData()
        }
    }

    fun updateUserIcons() {
        viewModelScope.launch{
            val icons = repository.getUserIcons()
            Log.i("HomeviewModel", "updateUserIcons called")
            Log.i("HomeviewModel", "firstUserIcon before: ${_firstUserIcon.value}")
            _firstUserIcon.value = icons[0]
            Log.i("HomeviewModel", "firstUserIcon after: ${_firstUserIcon.value}")
            _secondUserIcon.value = icons[1]
        }
    }

    fun postUserIcon(user: Int, uri: Uri) {
        viewModelScope.launch {
            repository.postUserIcon(user, uri)
            Log.i("HomeviewModel", "postUserIcons called")
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