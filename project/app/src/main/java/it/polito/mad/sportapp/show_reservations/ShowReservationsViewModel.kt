package it.polito.mad.sportapp.show_reservations

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.IRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/* View Model related to the Show Reservations Fragment */

@HiltViewModel
class ShowReservationsViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    // user reservations fire listener
    private var userReservationsFireListener: FireListener = FireListener()

    // mutable live data for the user events
    private var _userEvents =
        MutableLiveData<Map<LocalDate, List<DetailedReservation>>>().also {

            // get user id
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            userId?.let {
                // load user events from database
                userReservationsFireListener = loadEventsFromDb(userId)
            }
        }

    val userEvents: LiveData<Map<LocalDate, List<DetailedReservation>>> = _userEvents

    // mutable live data for the current month, the selected date and the previous selected date
    private val _currentMonth = MutableLiveData<YearMonth>().also { it.value = YearMonth.now() }
    private val _selectedDate = MutableLiveData<LocalDate>().also { it.value = LocalDate.now() }
    private val _previousSelectedDate = MutableLiveData<LocalDate>()

    // live data for the current month, the selected date and the previous selected date
    val currentMonth: LiveData<YearMonth> = _currentMonth
    val selectedDate: LiveData<LocalDate> = _selectedDate
    val previousSelectedDate: LiveData<LocalDate> = _previousSelectedDate

    /* error management */
    private var _getReservationsError = MutableLiveData<DefaultGetFireError?>()
    val getReservationsError: LiveData<DefaultGetFireError?> = _getReservationsError

    fun clearReservationsError() {
        _getReservationsError = MutableLiveData<DefaultGetFireError?>()
    }

    private fun loadEventsFromDb(uid: String): FireListener {

        // get user events from database
        return repository.getReservationsPerDateByUserId(uid) {
            when (it) {
                is FireResult.Error -> {
                    Log.e("ShowReservationsViewModel", it.errorMessage())
                    _getReservationsError.postValue(it.type)
                }

                is FireResult.Success -> {
                    // update user events
                    _userEvents.postValue(it.value)
                    Log.d("ShowReservationsViewModel", "User events updated successfully!")
                }
            }
        }
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

    override fun onCleared() {
        super.onCleared()

        // remove user reservations fire listener
        userReservationsFireListener.unregister()
    }

}