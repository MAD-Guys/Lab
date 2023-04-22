package it.polito.mad.sportapp.reservation_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.sportapp.localDB.DetailedReservation
import it.polito.mad.sportapp.model.Repository

class ReservationDetailsViewModel : ViewModel() {

    private val _reservation = MutableLiveData<DetailedReservation>().also {
        it.value = mockReservationDetails()
    }

    //set to true when the user is adding equipments
    private var dirty = false

    val reservation :LiveData<DetailedReservation> = _reservation
}
