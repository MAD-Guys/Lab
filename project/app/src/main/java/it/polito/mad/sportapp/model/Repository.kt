package it.polito.mad.sportapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.PlaygroundReservation
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.SportCenter
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.localDB.dao.EquipmentDao
import it.polito.mad.sportapp.localDB.dao.ReservationDao
import it.polito.mad.sportapp.localDB.dao.SportCenterDao
import it.polito.mad.sportapp.localDB.dao.SportDao
import it.polito.mad.sportapp.localDB.dao.UserDao
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
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

    fun getReservationPerDateByUserId(userId : Int) : MutableLiveData<Map<LocalDate, List<DetailedReservation>>>{
        val userReservations= reservationDao.findByUserId(userId)
        return MutableLiveData(userReservations.sortedBy { LocalDateTime.parse(it.startDateTime) }.groupBy { it.date })
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

    // * Playground methods *

    /* Get the available playgrounds for each slot in the provided month */
    fun getAvailablePlaygroundsIn(month: YearMonth, sport: Sport)
        : MutableLiveData<Map<LocalDateTime, List<DetailedPlaygroundSport>>> {
        /* temporary hardcoded data */
        val timeSlots = getRandomSlotsStartTimesIn(month, maxDaysOfMonth=20, maxSlots=15)
        val playgroundSports = getRandomPlaygroundSports(sport.id)

        return MutableLiveData(
                timeSlots.associateWith {
                playgroundSports.asSequence().shuffled()
                    .take(Random().ints(1, 8).iterator().next())
                    .toList()
            }
        )
    }


    /* hardcoded data utilities (to be deleted) */

    private fun getRandomSlotsStartTimesIn(
        month: YearMonth, maxDaysOfMonth: Int, maxSlots: Int
    ): List<LocalDateTime> {
        val randomDaysOfMonth = Random().ints(
            1,
            month.lengthOfMonth() + 1
        )

        return buildList {
            randomDaysOfMonth.limit(maxDaysOfMonth.toLong()).distinct().forEach { randomDay ->
                val randomDate = month.atDay(randomDay)

                // inspect random time slots in that date, starting from 9am to 21pm
                randomDate.atTime(9, 0).let {
                    val randomSlots = Random().longs(
                        0,
                        25
                    )

                    randomSlots.limit(maxSlots.toLong()).distinct().forEach { randomSlot ->
                        val randomSlotStartTime = it.plusMinutes(30L * randomSlot)
                        add(randomSlotStartTime)
                    }
                }
            }
        }
    }

    private fun getRandomPlaygroundSports(sportId: Int): List<DetailedPlaygroundSport> {
        val prices = listOf(10.00, 15.00, 20.00, 25.00)
        var nextId = 0
        val random1or2Generator = Random().longs(1,3).iterator()
        val randomSportCenterGenerator = Random().ints(0,3).iterator()
        val randomPriceGenerator = Random().ints(0, 4)
            .mapToDouble { index -> prices[index] }.iterator()
        val playgroundIds = IntRange(0, 15)
        val sports = listOf(
            "basket",
            "soccer11",
            "soccer5",
            "soccer8",
            "tennis",
            "tableTennis",
            "volleyball",
            "beachVolley",
            "padel",
            "miniGolf"
        )

        return buildList{
            playgroundIds.forEach{ playgroundId ->
                val randomSportIds = Random().ints(0, sports.size)

                randomSportIds.limit(random1or2Generator.next()).distinct()
                    .forEach { tempSportId ->
                        val tempSportName = sports[tempSportId]
                        val id = nextId++

                        //if(tempSportId == sportId) {
                        if(true) {
                            add(
                                DetailedPlaygroundSport(
                                    id,
                                    playgroundId,
                                    "Playground $playgroundId",
                                    tempSportName,
                                    "Sport center ${randomSportCenterGenerator.next()}",
                                    randomPriceGenerator.next().toFloat()
                                )
                            )
                        }
                    }
            }
        }
    }
}

data class DetailedPlaygroundSport(
    val id: Int,
    val playgroundId: Int,
    val playgroundName: String,
    val sportName: String,
    val sportCenterName: String,
    val pricePerHour: Float
)