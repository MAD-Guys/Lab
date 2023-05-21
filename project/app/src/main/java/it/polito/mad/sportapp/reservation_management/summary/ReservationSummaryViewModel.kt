package it.polito.mad.sportapp.reservation_management.summary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.model.IRepository
import it.polito.mad.sportapp.model.LocalRepository
import javax.inject.Inject

@HiltViewModel
class ReservationSummaryViewModel @Inject constructor(
    val repository: LocalRepository
) : ViewModel()
{
    // contains the reservation data to save in the db
    private val _reservation = MutableLiveData<NewReservation>()
    internal val reservation: LiveData<NewReservation> = _reservation

    /**
     * return null if everything went well, an error enum otherwise
     */
    fun permanentlySaveReservation(): Pair<Int?, IRepository.NewReservationError?> {
        var error: IRepository.NewReservationError? = null
        var id: Int? = null

        val t = Thread {
            val reservationToSave = reservation.value

            if(reservationToSave == null) {
                Log.d("permanentlySaveReservation_error", "trying to save a null reservation")
                return@Thread
            }

            // save data into db
            val (newReservationId, newReservationError) =
                repository.overrideNewReservation(reservationToSave)

            error = newReservationError
            id = newReservationId
        }
        // start secondary thread to save reservation
        t.start()

        // wait for it
        val timeout = 20000L // 20 sec
        try {
            t.join(timeout)
        }
        catch(e: InterruptedException) {
            // waited for too long
            error = IRepository.NewReservationError.UNEXPECTED_ERROR
        }

        return Pair(id, error)
    }

    fun setReservation(newReservation: NewReservation) {
        _reservation.value = newReservation
    }
}