package it.polito.mad.sportapp.reservation_management.equipments

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

@HiltViewModel
class ManageEquipmentsViewModel @Inject constructor(
    repository: Repository
) : ViewModel() {


}