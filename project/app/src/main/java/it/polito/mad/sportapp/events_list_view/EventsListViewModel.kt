package it.polito.mad.sportapp.events_list_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.model.Repository
import java.time.LocalDate
import javax.inject.Inject

/* View Model related to the Events List Fragment */

@HiltViewModel
class EventsListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    // mutable live data for the user events
    private var _userEvents = MutableLiveData<Map<LocalDate, List<DetailedReservation>>>()

    val userEvents: LiveData<Map<LocalDate, List<DetailedReservation>>> = _userEvents

    fun getUserEventsFromDb() {
        // get user events from database
        val dbThread = Thread {
            _userEvents.postValue(repository.getReservationPerDateByUserId(1))
        }

        // start db thread
        dbThread.start()
    }

}