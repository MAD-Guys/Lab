package it.polito.mad.sportapp.show_reservations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.model.Repository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/* View Model related to the Show Reservations Activity */

@HiltViewModel
class ShowReservationsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    // mutable live data for the user events
    private var _userEvents = MutableLiveData<Map<LocalDate, List<DetailedReservation>>>()

    val userEvents: LiveData<Map<LocalDate, List<DetailedReservation>>> = _userEvents

    // mutable live data for the current month, the selected date and the previous selected date
    private val _currentMonth = MutableLiveData<YearMonth>().also { it.value = YearMonth.now() }
    private val _selectedDate = MutableLiveData<LocalDate>().also { it.value = LocalDate.now() }
    private val _previousSelectedDate = MutableLiveData<LocalDate>()

    // live data for the current month, the selected date and the previous selected date
    val currentMonth: LiveData<YearMonth> = _currentMonth
    val selectedDate: LiveData<LocalDate> = _selectedDate
    val previousSelectedDate: LiveData<LocalDate> = _previousSelectedDate

    fun getUserEventsFromDb() {

        // get user events from database
        val dbThread = Thread {
            _userEvents.postValue(repository.getReservationPerDateByUserId(1))
        }

        // start db thread
        dbThread.start()
    }

    // add a month to the current month
    fun setCurrentMonth(month: YearMonth) {
        // update current month
        this._currentMonth.value = month
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