package it.polito.mad.sportapp.reservation_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

@HiltViewModel
class ReservationDetailsFragmentViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private var _reservation = MutableLiveData<DetailedReservation>()
    val reservation: LiveData<DetailedReservation> = _reservation

    fun getReservationFromDb(reservationId: Int) {

        // get reservation from database
        val dbThread = Thread {
            this._reservation.postValue(repository.getDetailedReservationById(reservationId))
        }

        // start db thread
        dbThread.start()
    }

    fun deleteReservation(): Boolean {

        // delete reservation in database
        val dbThread = Thread {
            repository.deleteReservation(_reservation.value!!)
        }

        // start db thread
        dbThread.start()

        return true
    }

}