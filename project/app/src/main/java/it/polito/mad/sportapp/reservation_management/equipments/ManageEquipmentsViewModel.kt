package it.polito.mad.sportapp.reservation_management.equipments

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.model.Repository
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ManageEquipmentsViewModel @Inject constructor(
    val repository: Repository
) : ViewModel()
{
    internal lateinit var reservationBundle: Bundle

    // all the (max) available equipments for that playground
    private val _availableEquipments = MutableLiveData<Map<Int, Equipment>>()
    internal val availableEquipments: LiveData<Map<Int, Equipment>> = _availableEquipments

    // current equipments selected by the user
    private val _selectedEquipments = MutableLiveData<MutableMap<Int, DetailedEquipmentReservation>>()
    internal val selectedEquipments: LiveData<MutableMap<Int, DetailedEquipmentReservation>> = _selectedEquipments


    internal fun loadEquipmentsQuantitiesAsync() {
        val sportCenterId = reservationBundle.getInt("sport_center_id")
        val sportId = reservationBundle.getInt("sport_id")
        val startSlot = reservationBundle.getString("start_slot")
        val endSlot = reservationBundle.getString("end_slot")
        val slotDurationMins = reservationBundle.getInt("slot_duration_mins")
        val reservationId = reservationBundle.getInt("reservation_id") // it may be null, if this is a new reservation

        Thread {
            // * retrieve all the available quantities left for each equipment *
            // in the specified sport center and sport, for the specified timeslots
            // • if reservationId != null, it must count this reservation equipments as available
            // • it has to return all the equipments, even with qty 0
            val availableEquipmentsQuantities = repository.getAvailableEquipmentsBySportCenterIdAndSportId(
                sportCenterId,
                sportId,
                reservationId,
                LocalDateTime.parse(startSlot),
                LocalDateTime.parse(endSlot).plusMinutes(slotDurationMins.toLong())
            )

            if (reservationId != 0) {
                // this is an *existing* reservation that is being *changed*

                if(_selectedEquipments.value != null) {
                    // maintain previous data, if any (e.g. when coming back from another view)
                    _selectedEquipments.postValue(_selectedEquipments.value)
                }
                else {
                    // * retrieve previous reservation and previous selected equipments *
                    val previousReservation = repository.getDetailedReservationById(reservationId)
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

                            val equipmentsToRemoveFromSelections = mutableListOf<Int>()

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
                }
            }
            else {
                // this is a new reservation (no previous equipments)
                // maintain previous selections, if any
                _selectedEquipments.postValue(_selectedEquipments.value ?: mutableMapOf()) // (empty map)
            }

            _availableEquipments.postValue(availableEquipmentsQuantities)
        }.start()
    }

    fun setSelectedEquipments(
        selectedEquipments: MutableMap<Int, DetailedEquipmentReservation>
    ) {
        this._selectedEquipments.value = selectedEquipments
    }
}