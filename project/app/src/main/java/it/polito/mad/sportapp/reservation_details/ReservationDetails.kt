package it.polito.mad.sportapp.reservation_details

//import it.polito.mad.sportapp.localDB.Equipment
//import it.polito.mad.sportapp.localDB.SportCenter
import java.time.LocalDate
import java.time.LocalTime

interface ReservationDetails {
    fun getId(): Int
    fun getUserName(): String
    fun getDate(): LocalDate
    fun getStartTime(): LocalTime
    fun getEndTime(): LocalTime
    fun getSport(): String
    fun getPlaygroundName(): String
    //fun getSportCenter(): SportCenter //TODO: change with the Entity
    //fun getEquipment(): List<Equipment> //TODO: change with the Entity
    fun getTotalPrice(): Float
}