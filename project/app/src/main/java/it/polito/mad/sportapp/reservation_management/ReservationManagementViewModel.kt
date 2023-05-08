package it.polito.mad.sportapp.reservation_management

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

@HiltViewModel
class ReservationManagementViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {


}