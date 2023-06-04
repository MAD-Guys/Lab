package it.polito.mad.sportapp.reservation_management.equipments

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult.*
import it.polito.mad.sportapp.model.IRepository
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ManageEquipmentsViewModel @Inject constructor(
    val repository: IRepository
) : ViewModel()
{
    internal lateinit var reservationBundle: Bundle

    // all the (max) available equipments for that playground
    private val _availableEquipments = MutableLiveData<Map<String, Equipment>>()
    internal val availableEquipments: LiveData<Map<String, Equipment>> = _availableEquipments

    internal var availableEquipmentsIndexesById: Map<String,Int>? = null
    internal var availableEquipmentsIdsByIndexes: Map<Int,String>? = null

    // current equipments selected by the user
    private val _selectedEquipments = MutableLiveData<MutableMap<String, DetailedEquipmentReservation>>()
    internal val selectedEquipments: LiveData<MutableMap<String, DetailedEquipmentReservation>> = _selectedEquipments

    internal val fireListener = FireListener()

    private var _toastErrorMessage = MutableLiveData<String>()
    internal var toastErrorMessage: LiveData<String> = _toastErrorMessage

    private val _equipmentsDataLoaded = MutableLiveData(false)
    internal val equipmentsDataLoaded: LiveData<Boolean> = _equipmentsDataLoaded

    fun clearToastErrorMessage() {
        _toastErrorMessage = MutableLiveData<String>()
        toastErrorMessage = _toastErrorMessage
    }

    internal fun loadEquipmentsQuantitiesAsync() {
        val sportCenterId = reservationBundle.getString("sport_center_id")!!
        val sportId = reservationBundle.getString("sport_id")!!
        val startSlot = reservationBundle.getString("start_slot")
        val endSlot = reservationBundle.getString("end_slot")
        val slotDurationMins = reservationBundle.getInt("slot_duration_mins")
        val reservationId = reservationBundle.getString("reservation_id") // it may be null, if this is a new reservation

        // * retrieve all the available quantities left for each equipment *
        // in the specified sport center and sport, for the specified timeslots
        // • if reservationId != null, it must count this reservation equipments as available
        // • it has to return all the equipments, even with qty 0
        val listener = repository.getAvailableEquipmentsBySportCenterIdAndSportId(
            sportCenterId,
            sportId,
            reservationId,
            LocalDateTime.parse(startSlot),
            LocalDateTime.parse(endSlot).plusMinutes(slotDurationMins.toLong())
        ) { fireResult ->
            when(fireResult) {
                is Error -> {
                    _toastErrorMessage.value = fireResult.errorMessage()
                    return@getAvailableEquipmentsBySportCenterIdAndSportId
                }
                is Success -> {
                    val availableEquipmentsQuantities = fireResult.value

                    if (reservationId != null) {
                        // this is an *existing* reservation that is being *changed*

                        if(_selectedEquipments.value != null) {
                            // maintain previous data, if any (e.g. when coming back from another view)
                            _selectedEquipments.postValue(_selectedEquipments.value)

                            _availableEquipments.postValue(availableEquipmentsQuantities)
                        }
                        else {
                            // * retrieve previous reservation and previous selected equipments *
                            val listener2 = repository.getDetailedReservationById(reservationId) { fireResult2 ->
                                when(fireResult2) {
                                    is Error -> {
                                        _toastErrorMessage.value = fireResult2.errorMessage()
                                        return@getDetailedReservationById
                                    }
                                    is Success -> {
                                        val previousReservation = fireResult2.value

                                        val previousSelectedEquipments = previousReservation.equipments.associateBy {
                                            it.equipmentId
                                        }.toMutableMap()

                                        // (if this reservation had not any equipments, nothing changes)
                                        if(previousSelectedEquipments.isNotEmpty()) {
                                            // Different (mutually exclusive) possibilities:
                                            // (1) sport center *did change* (regardless of slots changes)
                                            //    -> delete all previous equipments
                                            // (2) sport center did *not* change, but slots did
                                            //    -> check available equipments to maintain the possible equipments' quantities
                                            // (3) none of them changed

                                            if (previousReservation.sportCenterId != sportCenterId) {
                                                // (1) sport center changed

                                                // clear all previous selected equipments
                                                previousSelectedEquipments.clear()
                                            }
                                            else if (previousReservation.startSlot.toString() != startSlot ||
                                                previousReservation.endSlot.toString() != endSlot) {
                                                // (2) sport center did not change but slots changed

                                                // check if the selected equipments are among the available ones:
                                                // - if yes, adjust selected qty to be <= max available qty
                                                // - if not, remove them from selections

                                                val equipmentsToRemoveFromSelections = mutableListOf<String>()

                                                for ((equipmentId, selectedEquipment) in previousSelectedEquipments) {
                                                    if(availableEquipmentsQuantities.contains(equipmentId)) {
                                                        // adjust qty
                                                        val previousSelectedQty = selectedEquipment.selectedQuantity
                                                        val maxAvailableQty = availableEquipmentsQuantities[equipmentId]!!.availability

                                                        if (previousSelectedQty > maxAvailableQty) {
                                                            selectedEquipment.selectedQuantity = maxAvailableQty
                                                        }
                                                    }
                                                    else {
                                                        // remove it
                                                        equipmentsToRemoveFromSelections.add(equipmentId)
                                                    }
                                                }

                                                equipmentsToRemoveFromSelections.forEach{ equipmentId ->
                                                    previousSelectedEquipments.remove(equipmentId)
                                                }
                                            }
                                            // else {
                                            // (3) sport center did not change and slots neither
                                            //     * nothing to do in this case *
                                            // }
                                        }

                                        _selectedEquipments.postValue(previousSelectedEquipments)

                                        _availableEquipments.postValue(availableEquipmentsQuantities)
                                    }
                                }
                            }

                            fireListener.add(listener2)
                        }
                    }
                    else {
                        // this is a new reservation (no previous equipments)
                        // maintain previous selections, if any
                        _selectedEquipments.postValue(_selectedEquipments.value ?: mutableMapOf()) // (empty map)

                        _availableEquipments.postValue(availableEquipmentsQuantities)
                    }
                }
            }
        }

        fireListener.add(listener)
    }

    fun setSelectedEquipments(
        selectedEquipments: MutableMap<String, DetailedEquipmentReservation>
    ) {
        this._selectedEquipments.value = selectedEquipments
    }

    fun setEquipmentsDataLoaded() {
        this._equipmentsDataLoaded.value = true
    }
}