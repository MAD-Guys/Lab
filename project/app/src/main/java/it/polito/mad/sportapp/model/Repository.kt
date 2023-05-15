package it.polito.mad.sportapp.model

import android.util.Log
import it.polito.mad.sportapp.entities.Achievement
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.PlaygroundReservation
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.SportCenter
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.EquipmentReservation
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.NewReservationEquipment
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.localDB.dao.EquipmentDao
import it.polito.mad.sportapp.localDB.dao.PlaygroundSportDao
import it.polito.mad.sportapp.localDB.dao.ReservationDao
import it.polito.mad.sportapp.localDB.dao.ReviewDao
import it.polito.mad.sportapp.localDB.dao.SportCenterDao
import it.polito.mad.sportapp.localDB.dao.SportDao
import it.polito.mad.sportapp.localDB.dao.UserDao
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Repository @Inject constructor(
    private val userDao: UserDao,
    private val sportDao: SportDao,
    private val sportCenterDao: SportCenterDao,
    private val equipmentDao: EquipmentDao,
    private val reservationDao: ReservationDao,
    private val playgroundSportDao: PlaygroundSportDao,
    private val reviewDao: ReviewDao
) {
    // User methods
    fun getUser(id: Int): User {
        val user = userDao.findById(id)
        user.sportLevel = userDao.findSportByUserId(id)
        user.achievements = buildAchievements(id)
        return user
    }

    fun usernameAlreadyExists(username: String): Boolean {
        return userDao.findUsername(username) > 0
    }

    private fun buildAchievements(userId: Int): Map<Achievement, Boolean> {
        val playedMatches = userDao.findPlayedMatches(userId)
        val playedSport = userDao.findPlayedSports(userId).maxOrNull() ?: 0
        return mapOf(
            Achievement.AtLeastOneSport to (playedSport > 0),
            Achievement.AtLeastFiveSports to (playedSport > 4),
            Achievement.AllSports to (playedSport == sportDao.count()),
            Achievement.AtLeastThreeMatches to (playedMatches > 2),
            Achievement.AtLeastTenMatches to (playedMatches > 9),
            Achievement.AtLeastTwentyFiveMatches to (playedMatches > 24),
        )
    }

    fun updateUser(user: User) {
        userDao.deleteSportByUserId(user.id)
        user.sportLevel.forEach {
            userDao.insertSportByUserId(user.id, it.sportId, it.level ?: "")
        }
        userDao.update(user)
    }

    // Sport methods
    fun getAllSports(): List<Sport> {
        return sportDao.getAll()
    }


    // Review methods
    fun getAllReviewsByPlaygroundId(id: Int): List<Review> {
        val reviews = reviewDao.findByPlaygroundId(id)
        reviews.forEach {
            it.username = userDao.findUsernameById(it.userId)
        }
        return reviews
    }

    fun getReviewByUserIdAndPlaygroundId(userId: Int, playgroundId: Int): Review {
        val review = reviewDao.findByUserIdAndPlaygroundId(userId, playgroundId)
        review.username = userDao.findUsernameById(userId)
        return review
    }


    fun updateReview(review: Review) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).toString()
        review.lastUpdate = now
        if (review.id == 0) {
            review.timestamp = now
            reviewDao.insert(review)
        } else {
            reviewDao.update(review)
        }
    }

    fun deleteReview(review: Review) {
        reviewDao.delete(review)
    }

    // Reservation methods
    fun getAllReservations(): List<PlaygroundReservation> {
        return reservationDao.getAll()
    }

    fun getDetailedReservationById(id: Int): DetailedReservation {
        val reservation = reservationDao.findDetailedReservationById(id)
        reservation.equipments = equipmentDao.findReservationEquipmentsByReservationId(id).toMutableList()

        if (reservation.equipments.isEmpty()) {
            reservation.equipments = mutableListOf()
        }
        return reservation
    }

    fun getDetailedReservationBySportId(sportId: Int): List<DetailedReservation> {
        return reservationDao.findBySportId(sportId)
    }

    /**
     * Create a new reservation in the DB, or override the existing one if any (with the same reservationId)
     * Returns a Pair of (newReservationId, error), where:
     * - 'newReservationId' is the new id assigned to the reservation
     *  (or the same as the previous one, if any), if the save succeeds; 'null' otherwise
     * - 'error' is an instance of NewReservationError reflecting the type of error occurred, or 'null' otherwise
     */
    fun overrideNewReservation(reservation: NewReservation): Pair<Int?, NewReservationError?> {
        try {
            // Check slots availability
            if (equipmentsAreAvailable(
                    reservation.selectedEquipments,
                    reservation.playgroundId)
            ){
                // Delete equipments if any
                if (reservation.id != 0) {
                    equipmentDao.deleteEquipmentReservationByPlaygroundReservationId(reservation.id)
                }

                reservationDao.deleteById(reservation.id)

                if (slotsAreAvailable(
                        reservation.playgroundId,
                        reservation.startTime,
                        reservation.endTime
                )) {
                    val id = reservationDao.insert(
                        PlaygroundReservation(
                            0,
                            reservation.playgroundId,
                            1,
                            reservation.sportId,
                            reservation.sportCenterId,
                            reservation.startTime.format(DateTimeFormatter.ISO_DATE_TIME),
                            reservation.endTime.format(DateTimeFormatter.ISO_DATE_TIME),
                            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                            calculatePrice(reservation)
                        )
                    ).toInt()

                    reservation.selectedEquipments.forEach {
                        equipmentDao.insertEquipmentReservation(
                            EquipmentReservation(
                                0,
                                id,
                                it.equipmentId,
                                it.selectedQuantity,
                                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                                it.unitPrice * it.selectedQuantity
                            )
                        )
                    }
                    return Pair(id, null)
                }
                else {
                    return Pair(null, NewReservationError.EQUIPMENT_CONFLICT)
                }
            }
            else {
                return Pair(null, NewReservationError.SLOT_CONFLICT)
            }
        } catch (e: Exception) {
            Log.d("unexpected error", "(error)", e)
            return Pair(null, NewReservationError.UNEXPECTED_ERROR)
        }
    }

    private fun slotsAreAvailable(
        playgroundId: Int,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Boolean {
        return reservationDao.getReservationIfAvailable(
            playgroundId,
            startDateTime.toString(),
            endDateTime.toString()
        ) == 0
    }

    private fun equipmentsAreAvailable(
        equipments: List<NewReservationEquipment>,
        playgroundId: Int
    ): Boolean {
        // TODO check if the selected equipments are all available (for the selected playground, and the selected slot);
        return true

    }

    private fun calculatePrice(reservation: NewReservation): Float {
        val duration = Duration.between(reservation.startTime, reservation.endTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() - hours * 60
        val pricePerHour = playgroundSportDao.getPlaygroundSportPricePerHour(reservation.playgroundId)
        var cost = (hours * pricePerHour) + (minutes * pricePerHour / 60)
        reservation.selectedEquipments.forEach {
            cost += it.selectedQuantity * equipmentDao.getEquipmentUnitPrice(it.equipmentId)
        }
        return cost


    }

    enum class NewReservationError(val message: String) {
        SLOT_CONFLICT(
            "Ouch! the selected slots have just been booked by someone else \uD83D\uDE41. Please select new ones for your reservation!"
        ),
        EQUIPMENT_CONFLICT(
            "Ouch! the selected equipments have just been booked by someone else \uD83D\uDE41. Please select new ones for your reservation!"
        ),
        UNEXPECTED_ERROR(
            "An unexpected error occurred while saving your reservation. Please try again or check your connection status."
        )
    }

    fun getReservationsPerDateByUserId(userId: Int): Map<LocalDate, List<DetailedReservation>> {
        val userReservations = reservationDao.findByUserId(userId)
        return userReservations.sortedBy { LocalDateTime.parse(it.startDateTime) }
            .groupBy { it.date }
    }


    // * Equipment methods *

    fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: Int,
        sportId: Int
    ): MutableMap<Int, Equipment> {
        return equipmentDao
            .findBySportCenterIdAndSportId(sportCenterId, sportId)
            .associateBy { it.id }.toMutableMap()
    }

    fun getReservationEquipmentsQuantities(reservationId: Int): MutableMap<Int, DetailedEquipmentReservation> {
        return equipmentDao
            .findReservationEquipmentsByReservationId(reservationId)
            .associateBy { it.equipmentId }
            .toMutableMap()
    }

    fun addEquipmentReservation(equipment: Equipment, quantity: Int, playgroundReservationId: Int) {
        val equipmentReservation = EquipmentReservation(
            id = 0,
            equipmentId = equipment.id,
            playgroundReservationId = playgroundReservationId,
            quantity = quantity,
            totalPrice = equipment.price,
            timestamp = LocalDateTime.now().toString(),
        )
        reservationDao.increasePrice(playgroundReservationId, equipment.price * quantity)
        equipmentDao.insertEquipmentReservation(equipmentReservation)
    }

    fun updateEquipment(equipmentId: Int, quantity: Int, playgroundReservationId: Int) {
        val price = equipmentDao.findPriceById(equipmentId)
        if (quantity > 0) {
            equipmentDao.reduceAvailabilityOfN(equipmentId, quantity)
            equipmentDao.increaseQuantityOfN(
                equipmentId,
                playgroundReservationId,
                price * quantity,
                quantity
            )
            reservationDao.increasePrice(playgroundReservationId, price * quantity)
        } else if (quantity < 0) {
            equipmentDao.increaseAvailabilityOfN(equipmentId, -quantity)
            equipmentDao.reduceQuantityOfN(
                equipmentId,
                playgroundReservationId,
                price * -quantity,
                -quantity
            )
            reservationDao.reducePrice(playgroundReservationId, price * -quantity)
        }

    }

    fun deleteEquipmentReservation(equipmentReservation: EquipmentReservation) {
        equipmentDao.increaseAvailabilityOfN(
            equipmentReservation.equipmentId,
            equipmentReservation.quantity
        )
        reservationDao.reducePrice(
            equipmentReservation.playgroundReservationId,
            equipmentReservation.totalPrice
        )
        equipmentDao.deleteReservation(
            equipmentReservation.equipmentId,
            equipmentReservation.playgroundReservationId
        )
    }

    fun deleteReservation(reservation: DetailedReservation) {
        // * first, delete associated equipments *
        reservation.equipments.forEach {
            equipmentDao.deleteReservation(it.equipmentId, it.playgroundReservationId)
        }

        // * then, delete the reservation *
        reservationDao.deleteById(reservation.id)
    }

    // * Playground methods *

    fun getPlaygroundInfoById(playgroundId: Int): PlaygroundInfo {
        val playgroundInfo = playgroundSportDao.getPlaygroundInfo(playgroundId)
        playgroundInfo.reviewList = getAllReviewsByPlaygroundId(playgroundId)
        val overallQualityRating: Float =
            playgroundInfo.reviewList.map { it.qualityRating }.filter { it != 0f }.average()
                .toFloat()

        val overallFacilitiesRating: Float =
            playgroundInfo.reviewList.map { it.facilitiesRating }.filter { it != 0f }.average()
                .toFloat()
        playgroundInfo.overallQualityRating = overallQualityRating.takeIf { !it.isNaN() } ?: 0f
        playgroundInfo.overallFacilitiesRating =
            overallFacilitiesRating.takeIf { !it.isNaN() } ?: 0f
        playgroundInfo.overallRating = when {
            playgroundInfo.overallQualityRating == 0f -> playgroundInfo.overallFacilitiesRating
            playgroundInfo.overallFacilitiesRating == 0f -> playgroundInfo.overallQualityRating
            else -> (playgroundInfo.overallQualityRating + playgroundInfo.overallFacilitiesRating) / 2
        }
        return playgroundInfo
    }


    fun getAvailablePlaygroundsPerSlot(month: YearMonth, sport: Sport?)
            : MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>> {
        if (sport == null) {
            return mutableMapOf()
        }
        // * retrieve, for each timeslot in the month, the busy playgrounds *

        val busyPlaygrounds = reservationDao.findPlaygroundsBySportIdAndDate(
            sport.id,
            month.toString()
        )

        val busyPlaygroundsPerSlotAndDate = busyPlaygrounds.asSequence().map {
            Triple(
                it.startLocalDateTime,
                it.endLocalDateTime,
                DetailedPlaygroundSport(
                    it.playgroundId,
                    it.playgroundName,
                    it.sportId,
                    it.sportName,
                    it.sportCenterId,
                    it.sportCenterName,
                    it.pricePerHour
                )
            )
        }.flatMap { (slotStart, slotEnd, detailedPlaygroundSport) ->
            val playgroundsPerSlot = mutableListOf<Pair<LocalDateTime, DetailedPlaygroundSport>>()
            var tempSlot = slotStart

            while (tempSlot.isBefore(slotEnd)) {
                playgroundsPerSlot.add(tempSlot to detailedPlaygroundSport.clone())
                tempSlot = tempSlot.plusMinutes(30)
            }

            playgroundsPerSlot
        }
            .groupBy { it.first.toLocalDate()!! }
            .mapValues { (_, pairList) ->
                pairList.groupBy { (slot, _) ->
                    slot
                }
                    .mapValues { (_, pairList) ->
                        pairList.map { it.second }.toMutableList()
                    }
                    .toMutableMap()
            }
            .toMutableMap()

        // * retrieve all open playgrounds for each time slot (in a generic day) *

        val detailedPlaygrounds = playgroundSportDao.findBySportId(sport.id)

        val openPlaygroundsPerSlot = detailedPlaygrounds.asSequence().map {
            Triple(
                it.openingTime,
                it.closingTime,
                DetailedPlaygroundSport(
                    it.playgroundId,
                    it.playgroundName,
                    it.sportId,
                    it.sportName,
                    it.sportCenterId,
                    it.sportCenterName,
                    it.pricePerHour,
                    true
                )
            )
        }.flatMap { (slotStart, slotEnd, detailedPlaygroundSport) ->
            val playgroundsPerSport = mutableListOf<Pair<LocalTime, DetailedPlaygroundSport>>()
            var tempSlot = slotStart

            while (tempSlot.isBefore(slotEnd)) {
                playgroundsPerSport.add(tempSlot to detailedPlaygroundSport.clone())
                tempSlot = tempSlot.plusMinutes(30)
            }

            playgroundsPerSport
        }.groupBy { it.first }
            .mapValues { (_, pairList) ->
                pairList.map { (_, detailedPlayground) -> detailedPlayground }
            }

        val playgroundsPerSlotAndDate = busyPlaygroundsPerSlotAndDate


        for (day in 1..month.lengthOfMonth()) {
            openPlaygroundsPerSlot.forEach { (slot, playgrounds) ->
                val date = month.atDay(day)
                val slotDateTime = LocalDateTime.of(date, slot)

                if (!playgroundsPerSlotAndDate.containsKey(date))
                    playgroundsPerSlotAndDate[date] = mutableMapOf()

                val playgroundsPerSlot = playgroundsPerSlotAndDate[date]!!

                if (!playgroundsPerSlot.containsKey(slotDateTime)) {
                    // they are all available
                    playgroundsPerSlot[slotDateTime] =
                        mutableListOf<DetailedPlaygroundSport>().also { list ->
                            list.addAll(playgrounds.map {
                                // clone all the playgrounds instances
                                it.clone().apply {
                                    // mark them as available only if this is a future slot
                                    available = slotDateTime > LocalDateTime.now()
                                }
                            })
                        }
                } else {
                    val playgroundsInSlot = playgroundsPerSlot[slotDateTime]!!

                    playgroundsInSlot.addAll(
                        playgrounds.filter { playground ->
                            !playgroundsInSlot.contains(playground)
                        } // clone them
                            .map {
                                it.clone().apply {
                                    // mark them as available only if this is a future slot
                                    available = slotDateTime > LocalDateTime.now()
                                }
                            }
                    )
                }
            }
        }

        return playgroundsPerSlotAndDate
    }
}




