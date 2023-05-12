package it.polito.mad.sportapp.reservation_management

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

@HiltViewModel
class ReservationManagementViewModel @Inject constructor(
    repository: Repository
): ViewModel()
{
    // says if we are in add/edit mode or not
    internal var reservationManagementMode: ReservationManagementMode? = null

    // reservation data received from previous view
    private var _reservationBundle = MutableLiveData(Bundle())
    internal var reservationBundle: LiveData<Bundle> = _reservationBundle

    fun setReservationBundle(bundle: Bundle) {
        _reservationBundle.value = bundle
    }

    fun isStartTimeSet(): Boolean = reservationBundle.value?.getString("start_time") != null
    fun isEndTimeSet()  : Boolean = reservationBundle.value?.getString("end_time") != null
}