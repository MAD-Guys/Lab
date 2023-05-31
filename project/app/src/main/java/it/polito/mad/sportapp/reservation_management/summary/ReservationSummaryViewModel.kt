package it.polito.mad.sportapp.reservation_management.summary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.firestore.utilities.NewReservationError
import it.polito.mad.sportapp.model.IRepository
import javax.inject.Inject

@HiltViewModel
class ReservationSummaryViewModel @Inject constructor(
    val repository: IRepository
) : ViewModel()
{
    // contains the reservation data to save in the db
    private val _reservation = MutableLiveData<NewReservation>()
    internal val reservation: LiveData<NewReservation> = _reservation

    /**
     * return null if everything went well, an error enum otherwise
     */
    fun permanentlySaveReservation(
        returnCallback: (FireResult<String, NewReservationError>) -> Unit
    ) {
        val reservationToSave = reservation.value

        if(reservationToSave == null) {
            Log.d("permanentlySaveReservation_error", "trying to save a null reservation")
            returnCallback(NewReservationError.unexpected("trying to save a null reservation"))
            return
        }

        // save data into db
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        repository.overrideNewReservation(currentUserId, reservationToSave) { x ->
            returnCallback(x)   // manage it in the alert dialog
        }
    }

    fun setReservation(newReservation: NewReservation) {
        _reservation.value = newReservation
    }
}