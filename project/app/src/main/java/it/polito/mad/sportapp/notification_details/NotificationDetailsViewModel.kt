package it.polito.mad.sportapp.notification_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.model.LocalRepository
import javax.inject.Inject

/* View Model related to the Notification Details Fragment */

@HiltViewModel
class NotificationDetailsViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {

    // mutable live data and live data for reservation
    private val _reservation = MutableLiveData<DetailedReservation>()
    val reservation: LiveData<DetailedReservation> = _reservation

    fun getReservationFromDb(reservationId: Int) {
        Thread {
            val reservation = repository.getDetailedReservationById(reservationId)
            _reservation.postValue(reservation)
        }.start()
    }

}