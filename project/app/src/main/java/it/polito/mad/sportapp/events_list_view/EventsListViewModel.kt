package it.polito.mad.sportapp.events_list_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.model.Repository
import java.time.LocalDate
import javax.inject.Inject

/* View Model related to the Events List Activity */

@HiltViewModel
class EventsListViewModel @Inject constructor(
    repository: Repository
) : ViewModel() {

    // mutable live data for the user events
    private val _userEvents = MutableLiveData<Map<LocalDate, List<DetailedReservation>>>()

    val userEvents: LiveData<Map<LocalDate, List<DetailedReservation>>> = _userEvents

    fun getUserEventsFromDb() {
        // get events from database
        //_userEvents.value = repository.getDetailedReservationByUserId(1)
    }

}