package it.polito.mad.sportapp.playground_availabilities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.YearMonth

class PlaygroundAvailabilitiesViewModel : ViewModel() {
    // selected date
    private val _selectedDate = MutableLiveData(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    // current month
    private val _currentMonth = MutableLiveData(YearMonth.now())
    val currentMonth: LiveData<YearMonth> = _currentMonth

    fun setSelectedDate(newSelectedDate: LocalDate?) {
        // default selected date is the current date
        this._selectedDate.value = newSelectedDate ?: LocalDate.now()
    }

    fun setCurrentMonth(newCurrentMonth: YearMonth?) {
        this._currentMonth.value = newCurrentMonth
    }
}