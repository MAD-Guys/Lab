package it.polito.mad.sportapp.show_reservations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.YearMonth

class ShowReservationsViewModel : ViewModel() {

    // mutable live data for the current month and the selected day
    private val _currentMonth = MutableLiveData<YearMonth>().also { it.value = YearMonth.now() }
    private val _selectedDate = MutableLiveData<LocalDate>().also { it.value = LocalDate.now() }

    // live data for the current month and the selected day
    val currentMonth: LiveData<YearMonth> = _currentMonth
    val selectedDate: LiveData<LocalDate> = _selectedDate

    // add a month to the current month
    fun setCurrentMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    // set a new selected date or the current date if date is null
    fun setSelectedDate(date: LocalDate?) {
        _selectedDate.value = date ?: LocalDate.now()
    }

}