package it.polito.mad.sportapp.model

import android.app.Application
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.localDB.AppDatabase
import it.polito.mad.sportapp.localDB.DetailedReservation
import it.polito.mad.sportapp.entities.PlaygroundReservation
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.SportCenter
import it.polito.mad.sportapp.entities.User

class Repository(application: Application) {
    private val userDao = AppDatabase.getInstance(application)?.userDao()
    private val sportDao = AppDatabase.getInstance(application)?.sportDao()
    private val sportCenterDao = AppDatabase.getInstance(application)?.sportCenterDao()
    private val equipmentDao = AppDatabase.getInstance(application)?.equipmentDao()
    private val reservationDao = AppDatabase.getInstance(application)?.reservationDao()

    // User methods
    fun getAllUsers(): List<User>? {
        return userDao?.getAll()
    }

    fun getUserWithSportLevel(id: Int): User? {
        val user = userDao?.findById(id)
        if (user != null) {
            user.sportLevel = userDao?.findSportByUserId(id) ?: listOf()
        }
        return user
    }

    fun insertUser(user: User) {
        userDao?.insert(user)
    }

    fun updateUser(user: User) {
        userDao?.update(user)
    }

    // Sport methods
    fun getAllSports(): List<Sport>? {
        return sportDao?.getAll()
    }

    // Reservation methods
    fun getAllReservations(): List<PlaygroundReservation>? {
        return reservationDao?.getAll()
    }

    fun getDetailedReservationById(id: Int): DetailedReservation? {
        val reservation = reservationDao?.findDetailedReservationById(id)
        reservation?.equipments = reservationDao?.findEquipmentByReservationId(id) ?: listOf()
        return reservation
    }

    fun getReservationBySportId(sportId: Int): List<DetailedReservation>? {
        return reservationDao?.findBySportId(sportId)
    }

    // Sport Center methods
    fun getAllSportCenter(): List<SportCenter>{
        return sportCenterDao?.getAll() ?: listOf()
    }

    fun getSportCenterById(id: Int): SportCenter? {
        return sportCenterDao?.findById(id)
    }

    fun getSportCenterByName(name: String): SportCenter? {
        return sportCenterDao?.findByName(name)
    }

    // Equipment methods
    fun getEquipmentBySportCenterId(sportCenterId: Int): List<Equipment>? {
        return equipmentDao?.findBySportCenterId(sportCenterId)
    }

    fun getEquipmentBySportCenterIdAndSportId(sportCenterId: Int, sportId: Int): List<Equipment>? {
        return equipmentDao?.findBySportCenterIdAndSportId(sportCenterId, sportId)
    }



    

}