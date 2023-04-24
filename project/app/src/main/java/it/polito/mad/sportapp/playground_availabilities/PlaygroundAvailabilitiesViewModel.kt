package it.polito.mad.sportapp.playground_availabilities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.YearMonth

class PlaygroundAvailabilitiesViewModel : ViewModel() {
    // previous selected date
    private val _previousSelectedDate = MutableLiveData<LocalDate>()
    val previousSelectedDate: LiveData<LocalDate> = _previousSelectedDate

    // selected date
    private val _selectedDate = MutableLiveData(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    // current month
    private val _currentMonth = MutableLiveData(YearMonth.now())
    val currentMonth: LiveData<YearMonth> = _currentMonth

    fun setSelectedDate(newSelectedDate: LocalDate?) {
        val tempPreviousSelectedDate = this.selectedDate.value

        // default selected date is the current date
        this._selectedDate.value = newSelectedDate ?: LocalDate.now()
        // update previous selected date
        this._previousSelectedDate.value = tempPreviousSelectedDate
    }

    fun setCurrentMonth(month: YearMonth) {
        _currentMonth.value = month
    }
}