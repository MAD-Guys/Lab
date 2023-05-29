package it.polito.mad.sportapp.reservation_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.room.RoomDetailedReservation
import it.polito.mad.sportapp.model.FireRepository
import it.polito.mad.sportapp.model.IRepository
import it.polito.mad.sportapp.model.LocalRepository
import javax.inject.Inject

@HiltViewModel
class ReservationDetailsViewModel @Inject constructor(
    //private val iRepository: IRepository,
    private val repository: LocalRepository
) : ViewModel() {

    /**
     * TODO:
     * (1) Get the correct reservationId:String from showReservations view
     * (2) fix ReservationManagementUtilities.createBundleFrom in order to make it accept a DetailedReservation object
     * (3) put iRepository in the constructor
     * */

    //TODO: put it in thr constructor
    private var iRepository: IRepository = FireRepository()

    private var _reservation = MutableLiveData<DetailedReservation>()
    val reservation: LiveData<DetailedReservation> = _reservation

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun getReservationFromDb(reservationId: String) : FireListener {
        return iRepository.getDetailedReservationById(reservationId){ fireResult ->
            when(fireResult){
                is FireResult.Error -> {
                    println(fireResult.errorMessage())
                }
                is FireResult.Success -> {
                    this._reservation.postValue(fireResult.value)
                }
            }
        }
    }

    fun deleteReservation(): Boolean {
        iRepository.deleteReservation(_reservation.value!!){fireResult ->
            when(fireResult){
                is FireResult.Error -> Log.d(fireResult.type.message(), fireResult.errorMessage())
                is FireResult.Success -> {}
            }
        }
        return true
    }
}