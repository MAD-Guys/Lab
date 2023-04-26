package it.polito.mad.sportapp.show_reservations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.model.Repository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/* View Model related to the Show Reservations Activity */

@HiltViewModel
class ShowReservationsViewModel @Inject constructor(
    repository: Repository
) : ViewModel() {

    // mutable live data for the current month, the selected date and the previous selected date
    private val _currentMonth = MutableLiveData<YearMonth>().also { it.value = YearMonth.now() }
    private val _previousMonth = MutableLiveData<YearMonth>()
    private val _selectedDate = MutableLiveData<LocalDate>().also { it.value = LocalDate.now() }
    private val _previousSelectedDate = MutableLiveData<LocalDate>()

    // live data for the current month, the selected date and the previous selected date
    val currentMonth: LiveData<YearMonth> = _currentMonth
    val previousMonth: LiveData<YearMonth> = _previousMonth
    val selectedDate: LiveData<LocalDate> = _selectedDate
    val previousSelectedDate: LiveData<LocalDate> = _previousSelectedDate

    init {
    }

    // add a month to the current month
    fun setCurrentMonth(month: YearMonth) {

        val tempPreviousMonth = this._currentMonth.value

        // update current month
        this._currentMonth.value = month
        //update previous month
        this._previousMonth.value = tempPreviousMonth
    }

    // set a new selected date or the current date if date is null
    fun setSelectedDate(date: LocalDate?) {

        val tempPreviousSelectedDate = this._selectedDate.value

        // update selected date
        this._selectedDate.value = date ?: LocalDate.now()
        // update previous selected date
        this._previousSelectedDate.value = tempPreviousSelectedDate
    }

}