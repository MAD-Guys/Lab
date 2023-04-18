package it.polito.mad.sportapp.reservation_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReservationDetailsViewModel : ViewModel() {

    private val _reservation = MutableLiveData<ReservationDetails>().also {
        it.value = MockReservationDetails()
    }

    val reservation :LiveData<ReservationDetails> = _reservation
}
