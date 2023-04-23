package it.polito.mad.sportapp.model

import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.PlaygroundReservation
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.SportCenter
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.localDB.DetailedReservation
import it.polito.mad.sportapp.localDB.dao.EquipmentDao
import it.polito.mad.sportapp.localDB.dao.ReservationDao
import it.polito.mad.sportapp.localDB.dao.SportCenterDao
import it.polito.mad.sportapp.localDB.dao.SportDao
import it.polito.mad.sportapp.localDB.dao.UserDao
import javax.inject.Inject


class Repository @Inject constructor(
    private val userDao: UserDao,
    private val sportDao: SportDao,
    private val sportCenterDao: SportCenterDao,
    private val equipmentDao: EquipmentDao,
    private val reservationDao: ReservationDao
) {

    // User methods
    fun getAllUsers(): List<User> {
        return userDao.getAll()
    }

    fun getUserWithSportLevel(id: Int): User {
        val user = userDao.findById(id)
        user.sportLevel = userDao.findSportByUserId(id)
        return user
    }

    fun insertUser(user: User) {
        userDao.insert(user)
    }

    fun updateUser(user: User) {
        userDao.update(user)
    }

    // Sport methods
    fun getAllSports(): List<Sport> {
        return sportDao.getAll()
    }

    // Reservation methods
    fun getAllReservations(): List<PlaygroundReservation> {
        return reservationDao.getAll()
    }

    fun getDetailedReservationById(id: Int): DetailedReservation {
        val reservation = reservationDao.findDetailedReservationById(id)
        reservation.equipments = reservationDao.findEquipmentByReservationId(id)
        return reservation
    }

    fun getReservationBySportId(sportId: Int): List<DetailedReservation> {
        return reservationDao.findBySportId(sportId)
    }

    // Sport Center methods
    fun getAllSportCenter(): List<SportCenter> {
        return sportCenterDao.getAll()
    }

    fun getSportCenterById(id: Int): SportCenter {
        return sportCenterDao.findById(id)
    }

    fun getSportCenterByName(name: String): SportCenter {
        return sportCenterDao.findByName(name)
    }

    // Equipment methods
    fun getEquipmentBySportCenterId(sportCenterId: Int): List<Equipment> {
        return equipmentDao.findBySportCenterId(sportCenterId)
    }

    fun getEquipmentBySportCenterIdAndSportId(sportCenterId: Int, sportId: Int): List<Equipment> {
        return equipmentDao.findBySportCenterIdAndSportId(sportCenterId, sportId)
    }


}