package it.polito.mad.sportapp.reservation_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.EquipmentReservation
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

@HiltViewModel
class EditEquipmentViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private var _reservation = MutableLiveData<DetailedReservation>()
    val reservation: LiveData<DetailedReservation> = _reservation

    private var _availableEquipment: MutableList<Equipment> = mutableListOf()
    private var isAvailableListInitialized = false

    //items to display in the "available equipments" section: remove the items already selected

    private val _tempAvailableEquipment = MutableLiveData<MutableList<Equipment>>()

    val tempAvailableEquipment: MutableLiveData<MutableList<Equipment>> = _tempAvailableEquipment

    private val _tempSelectedEquipment = MutableLiveData<MutableList<EquipmentReservation>>()
    private var isSelectedEquipmentInitialized = false

    val tempSelectedEquipment: MutableLiveData<MutableList<EquipmentReservation>> =
        _tempSelectedEquipment

    var tempPrice: MutableLiveData<Float> = MutableLiveData<Float>()
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

        tempPrice.value = r.totalPrice
        val dbThread = Thread {
            this._tempAvailableEquipment.postValue(
                repository.getEquipmentBySportCenterIdAndSportId(
                    r.sportCenterId,
                    r.sportId
                )
            )
        }
        dbThread.start()
    }

    fun initAvailableEquipments() {

        if (
            !isAvailableListInitialized
            && _tempAvailableEquipment.value?.isNotEmpty() == true
        ) {
            _availableEquipment.addAll(_tempAvailableEquipment.value!!)

            _tempAvailableEquipment.value!!.filter {
                var result = true
                for (e in _reservation.value?.equipments!!) {
                    if (it.id == e.equipmentId) result = false
                }
                return@filter result
            }
            isAvailableListInitialized = true
        }
    }

    fun initSelectedEquipments() {
        if (
            !isSelectedEquipmentInitialized
            && _reservation.value?.equipments?.isNotEmpty() == true
        ) {
            _tempSelectedEquipment.postValue(
                _reservation.value?.equipments?.toMutableList() ?: mutableListOf()
            )
        }
        isSelectedEquipmentInitialized = true
    }

    fun addEquipment(
        playgroundReservationId: Int,
        equipmentId: Int,
        price: Float,
        equipmentName: String
    ) {
        if (_tempSelectedEquipment.value == null) {
            _tempSelectedEquipment.value = mutableListOf()
        }
        val newReservation = EquipmentReservation(
            -1,
            playgroundReservationId,
            equipmentId,
            1,
            "timestamp",
            price
        )
        newReservation.equipmentName = equipmentName
        _tempSelectedEquipment.value!!.add(
            newReservation
        )
        _tempAvailableEquipment.value?.removeIf { it.id == equipmentId }
        tempPrice.value = tempPrice.value?.plus(price) ?: price

        tempAvailableEquipment.value = _tempAvailableEquipment.value
        tempSelectedEquipment.value = _tempSelectedEquipment.value
    }

    private fun removeEquipment(equipmentId: Int) {
        val index =
            _tempSelectedEquipment.value!!.indexOf(_tempSelectedEquipment.value!!.find { it.equipmentId == equipmentId })

        removed.add(_tempSelectedEquipment.value!![index].id)

        _tempAvailableEquipment.value?.add(_availableEquipment.find { it.id == equipmentId }!!)

        tempPrice.value = tempPrice.value?.minus(_tempSelectedEquipment.value!![index].totalPrice)
            ?: tempPrice.value

        _tempSelectedEquipment.value!!.removeAt(index)

        tempAvailableEquipment.value = _tempAvailableEquipment.value
        tempSelectedEquipment.value = _tempSelectedEquipment.value
    }

    fun incrementQuantity(equipmentId: Int): Boolean {
        var result = true
        val current = _tempSelectedEquipment.value!!.find { it.equipmentId == equipmentId }
        val index = _tempSelectedEquipment.value!!.indexOf(current)
        val newPrice = (current!!.totalPrice / current.quantity) * (current.quantity + 1)
        tempPrice.value =
            tempPrice.value?.minus(current.totalPrice)?.plus(newPrice) ?: tempPrice.value

        if (current.quantity < _availableEquipment.find { it.id == equipmentId }!!.availability) {
            val newReservation = EquipmentReservation(
                current.id,
                current.playgroundReservationId,
                equipmentId,
                current.quantity + 1,
                current.timestamp,
                newPrice
            )
            newReservation.equipmentName = current.equipmentName
            _tempSelectedEquipment.value!![index] = newReservation

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
            val newPrice = (current.totalPrice / current.quantity) * (current.quantity - 1)
            tempPrice.value =
                tempPrice.value?.minus(current.totalPrice)?.plus(newPrice) ?: tempPrice.value
            val newReservation = EquipmentReservation(
                current.id,
                current.playgroundReservationId,
                equipmentId,
                current.quantity - 1,
                current.timestamp,
                newPrice
            )
            newReservation.equipmentName = current.equipmentName
            _tempSelectedEquipment.value!![index] = newReservation

            dirty.add(current.id)
        } else {
            removeEquipment(equipmentId) //when quantity becomes zero
        }

        tempSelectedEquipment.value = _tempSelectedEquipment.value
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

            tempSelectedEquipment.value?.let {
                //add the added
                for (e in tempSelectedEquipment.value!!) {
                    if (e.id == -1) { //if the item was not already saved
                        //call repository to add
                        val equipment = _availableEquipment.find { it.id == e.equipmentId }

                        equipment?.let {
                            repository.addEquipmentReservation(
                                equipment,
                                e.quantity,
                                e.playgroundReservationId
                            )
                        }
                    }
                }
            }
        }

        dbThread.start()
    }

}