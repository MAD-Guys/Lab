package it.polito.mad.sportapp.reservation_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.EquipmentReservation
import it.polito.mad.sportapp.entities.PlaygroundReservation
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

@HiltViewModel
class EditEquipmentViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private var _reservation = MutableLiveData<DetailedReservation>()
    val reservation: LiveData<DetailedReservation> = _reservation

    /*
    private val _availableEquipment = MutableLiveData<MutableList<Equipment>>().also {
        it.value = mockAvailableEquipment()
    }

     */
    private val _availableEquipment = MutableLiveData<MutableList<Equipment>>()

    //items to display in the "available equipments" section: remove the items already selected

    private val _tempAvailableEquipment: MutableLiveData<MutableList<Equipment>> =
        MutableLiveData<MutableList<Equipment>>(
            _availableEquipment.value?.filter {
                var result = true
                for (e in _reservation.value?.equipments!!) {
                    if (it.id == e.equipmentId) result = false
                }
                return@filter result
            }?.toMutableList()
        )


    //private val _tempAvailableEquipment: MutableLiveData<MutableList<Equipment>> = MutableLiveData<MutableList<Equipment>>()
    val tempAvailableEquipment: MutableLiveData<MutableList<Equipment>> = _tempAvailableEquipment


    private val _tempSelectedEquipment: MutableLiveData<MutableList<EquipmentReservation>> =
        MutableLiveData<MutableList<EquipmentReservation>>(
            _reservation.value?.equipments?.toMutableList()
        )


    //private val _tempSelectedEquipment: MutableLiveData<MutableList<EquipmentReservation>> = MutableLiveData<MutableList<EquipmentReservation>>()
    val tempSelectedEquipment: MutableLiveData<MutableList<EquipmentReservation>> =
        _tempSelectedEquipment

    var tempPrice: MutableLiveData<Float> = MutableLiveData<Float>(_reservation.value?.totalPrice)
    private val dirty: MutableSet<Int> = mutableSetOf()
    private val removed: MutableSet<Int> = mutableSetOf()

    fun getReservationFromDb(reservationId: Int) {

        // get reservation from database
        val dbThread = Thread {
            this._reservation.postValue(repository.getDetailedReservationById(reservationId))
        }

        // start db thread
        dbThread.start()
    }

    fun getEquipmentFromDb(r: DetailedReservation) {

        val dbThread = Thread {
            this._availableEquipment.postValue(
                repository.getEquipmentBySportCenterIdAndSportId(
                    r.sportCenterId,
                    r.sportId
                )
            )
            /*
            _tempAvailableEquipment.postValue(
                (_availableEquipment.value?.filter {
                    var result = true
                    for (e in _reservation.value?.equipments!!) {
                        if (it.id == e.equipmentId) result = false
                    }
                    return@filter result
                } as MutableList<Equipment>)
            )
            _tempSelectedEquipment.postValue(
                _reservation.value?.equipments as MutableList<EquipmentReservation>
            )
            */

        }
        dbThread.start()
    }

    fun addEquipment(playgroundReservationId: Int, equipmentId: Int, price: Float) {
        _tempSelectedEquipment.value!!.add(
            EquipmentReservation(
                -1,
                playgroundReservationId,
                equipmentId,
                1,
                "timestamp",
                price
            )
        )
        _tempAvailableEquipment.value?.removeIf { it.id == equipmentId }
        tempPrice.value = tempPrice.value?.plus(price) ?: price

        tempAvailableEquipment.setValue(_tempAvailableEquipment.value)
        tempSelectedEquipment.setValue(_tempSelectedEquipment.value)
    }

    private fun removeEquipment(equipmentId: Int) {
        val index =
            _tempSelectedEquipment.value!!.indexOf(_tempSelectedEquipment.value!!.find { it.equipmentId == equipmentId })
        removed.add(_tempSelectedEquipment.value!![index].id)
        _tempAvailableEquipment.value?.add(_availableEquipment.value?.find { it.id == equipmentId }!!)
        tempPrice.value = tempPrice.value?.minus(_tempSelectedEquipment.value!![index].totalPrice)
            ?: tempPrice.value
        _tempSelectedEquipment.value!!.removeAt(index)

        tempAvailableEquipment.setValue(_tempAvailableEquipment.value)
        tempSelectedEquipment.setValue(_tempSelectedEquipment.value)
    }

    fun incrementQuantity(equipmentId: Int): Boolean {
        var result = true
        val current = _tempSelectedEquipment.value!!.find { it.equipmentId == equipmentId }
        val index = _tempSelectedEquipment.value!!.indexOf(current)
        val newPrice = (current!!.totalPrice / current.quantity) * (current.quantity + 1)
        tempPrice.value =
            tempPrice.value?.minus(current!!.totalPrice)?.plus(newPrice) ?: tempPrice.value

        if (current.quantity < _availableEquipment.value?.find { it.id == equipmentId }!!.availability) {
            _tempSelectedEquipment.value!![index] = EquipmentReservation(
                current.id,
                current.playgroundReservationId,
                equipmentId,
                current.quantity + 1,
                current.timestamp,
                newPrice
            )

            dirty.add(current.id)
            tempSelectedEquipment.setValue(_tempSelectedEquipment.value)
        } else {
            result = false
        }
        return result
    }

    fun decrementQuantity(equipmentId: Int) {
        val current = _tempSelectedEquipment.value!!.find { it.equipmentId == equipmentId }

        if (current!!.quantity > 1) {
            val index = _tempSelectedEquipment.value!!.indexOf(current)
            val newPrice = (current!!.totalPrice / current.quantity) * (current.quantity - 1)
            tempPrice.value =
                tempPrice.value?.minus(current.totalPrice)?.plus(newPrice) ?: tempPrice.value
            _tempSelectedEquipment.value!![index] = EquipmentReservation(
                current.id,
                current!!.playgroundReservationId,
                equipmentId,
                current!!.quantity - 1,
                current.timestamp,
                newPrice
            )

            dirty.add(current.id)
        } else {
            removeEquipment(equipmentId) //when quantity becomes zero
        }
        tempSelectedEquipment.setValue(_tempSelectedEquipment.value)
    }

    fun saveEquipment() {

        val dbThread = Thread {
            //Remove the removed
            for (i in removed) {
                if (i != -1) { //if the item was already saved
                    //call repository to remove
                    val equipment = _tempSelectedEquipment.value!!.find { it.id == i }
                    repository.deleteEquipmentReservation(equipment!!)
                }
            }
            //update the updated
            for (i in dirty) {
                if (i != -1) { //if the item was already saved
                    //call repository to update
                    val equipment = _tempSelectedEquipment.value!!.find { it.id == i }
                    val old = _reservation.value!!.equipments.find { it.id == i }
                    repository.updateEquipment(
                        equipment!!.id,
                        equipment.quantity - old!!.quantity,
                        equipment.playgroundReservationId
                    )
                }
            }
            //add the added
            for (e in tempSelectedEquipment.value!!) {
                if (e.id == -1) { //if the item was not already saved
                    //call repository to add
                    val equipment = _availableEquipment.value!!.find { it.id == e.equipmentId }
                    repository.addEquipmentReservation(
                        equipment!!,
                        e.quantity,
                        e.playgroundReservationId
                    )
                }
            }
        }
        dbThread.start()
    }

}