package it.polito.mad.sportapp.reservation_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.IRepository
import javax.inject.Inject

@HiltViewModel
class ReservationDetailsViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    private var _reservation = MutableLiveData<DetailedReservation>()
    val reservation: LiveData<DetailedReservation> = _reservation

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    private var _getError = MutableLiveData<DefaultGetFireError?>()
    val getError: LiveData<DefaultGetFireError?> = _getError

    private var _deleteError = MutableLiveData<DefaultFireError?>()
    val deleteError: LiveData<DefaultFireError?> = _deleteError

    fun clearReservationDetailsErrors() {
        _getError = MutableLiveData<DefaultGetFireError?>()
        _deleteError = MutableLiveData<DefaultFireError?>()
    }

    private var _deleteSuccess = MutableLiveData<Boolean?>()
    val deleteSuccess: LiveData<Boolean?> = _deleteSuccess

    fun getReservationFromDb(reservationId: String) : FireListener {
        return repository.getDetailedReservationById(reservationId){ fireResult ->
            when(fireResult){
                is FireResult.Error -> {
                    Log.e(fireResult.type.message(), fireResult.errorMessage())
                    _getError.postValue(fireResult.type)
                }
                is FireResult.Success -> {
                    this._reservation.postValue(fireResult.value)
                }
            }
        }
    }

    fun deleteReservation() {
        repository.deleteReservation(_reservation.value!!){fireResult ->
            when(fireResult){
                is FireResult.Error -> {
                    Log.e(fireResult.type.message(), fireResult.errorMessage())
                    _deleteError.postValue(fireResult.type)
                }
                is FireResult.Success -> {
                    _deleteSuccess.postValue(true)
                }
            }
        }
    }
}