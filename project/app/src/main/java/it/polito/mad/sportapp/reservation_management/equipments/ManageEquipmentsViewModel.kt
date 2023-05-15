package it.polito.mad.sportapp.reservation_management.equipments

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.model.Repository
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

        Thread {
            // * retrieve all the available quantities left for each equipment *
            val availableEquipmentsQuantities = repository.getAvailableEquipmentsBySportCenterIdAndSportId(
                sportCenterId,
                sportId
            )

            val reservationId = reservationBundle.getInt("reservation_id")

            if (reservationId != 0) {
                // this is an *existing* reservation that is being *edited*
                // * retrieve previous selected equipments *
                val reservationEquipmentsQuantities = repository.getReservationEquipmentsQuantities(
                    reservationId
                )

                if (_selectedEquipments.value == null)
                    _selectedEquipments.postValue(reservationEquipmentsQuantities)
                else // maintain previous data, if any (e.g.) when coming back from another view
                    _selectedEquipments.postValue(_selectedEquipments.value)

                // increment equipments' availabilities by the selected equipments' ones
                for ((_, selectedEquipment) in reservationEquipmentsQuantities) {
                    val equipment = Equipment(
                        selectedEquipment.equipmentId,
                        selectedEquipment.equipmentName,
                        reservationBundle.getInt("sport_id"),
                        reservationBundle.getInt("sport_center_id"),
                        selectedEquipment.unitPrice,
                        0
                    )

                    if (!availableEquipmentsQuantities.containsKey(selectedEquipment.equipmentId)) {
                        // this selected equipments quantities are the only available left
                        // set a new equipment having that availability
                        equipment.availability = selectedEquipment.selectedQuantity    // left availability coincides with the selected qty

                        availableEquipmentsQuantities[selectedEquipment.equipmentId] = equipment
                    }
                    else {
                        // increment availability of this equipment
                        val oldQty = availableEquipmentsQuantities[equipment.id]!!.availability
                        availableEquipmentsQuantities[equipment.id]!!.availability = oldQty + selectedEquipment.selectedQuantity
                    }
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