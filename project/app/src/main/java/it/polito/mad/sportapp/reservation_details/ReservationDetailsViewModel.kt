package it.polito.mad.sportapp.reservation_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
     * 1) Delete the commented lines
     * 2) Get the correct reservationId:String from showReservations view
     * 3) fix ReservationManagementUtilities.createBundleFrom in order to make it accept a DetailedReservation object
     * 4) put iRepository in the constructor
     * */

    //TODO: put it in thr constructor
    private var iRepository: IRepository = FireRepository()

    /*
    private var _reservation = MutableLiveData<RoomDetailedReservation>()
    val reservation: LiveData<RoomDetailedReservation> = _reservation
     */
    private var _reservation = MutableLiveData<DetailedReservation>()
    val reservation: LiveData<DetailedReservation> = _reservation

    /*
    private var _participants = MutableLiveData<List<String>>() //TODO: get something different from db
    val participants: LiveData<List<String>> = _participants
     */

    /*
    fun getReservationFromDb(reservationId: Int) {

        // get reservation from database
        val dbThread = Thread {
            this._reservation.postValue(repository.getDetailedReservationById(reservationId))
        }

        // start db thread
        dbThread.start()
    }
     */
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

    /*
    fun deleteReservation(): Boolean {

        // delete reservation in database
        val dbThread = Thread {
            repository.deleteReservation(_reservation.value!!)
        }

        // start db thread
        dbThread.start()

        return true
    }
     */
    fun deleteReservation(): Boolean {
        iRepository.deleteReservation(_reservation.value!!){fireResult ->
            when(fireResult){
                is FireResult.Error -> {}
                is FireResult.Success -> {}
            }
        }
        return true
    }

    /*
    fun getParticipants(reservationId: Int){
        this._participants.postValue(
            listOf(
                "michelepistan", "fraros", "peppelazzara", "mariomastrandrea"
            )
        )
    }

     */

}