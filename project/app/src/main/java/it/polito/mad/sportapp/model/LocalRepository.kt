package it.polito.mad.sportapp.model

import android.util.Log
import it.polito.mad.sportapp.entities.Achievement
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.PlaygroundReservation
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.EquipmentReservation
import it.polito.mad.sportapp.entities.EquipmentReservationForAvailabilities
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.NewReservationEquipment
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.localDB.dao.EquipmentDao
import it.polito.mad.sportapp.localDB.dao.PlaygroundSportDao
import it.polito.mad.sportapp.localDB.dao.ReservationDao
import it.polito.mad.sportapp.localDB.dao.ReviewDao
import it.polito.mad.sportapp.localDB.dao.SportDao
import it.polito.mad.sportapp.localDB.dao.UserDao
import it.polito.mad.sportapp.entities.firestore.utilities.NewReservationError
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocalRepository @Inject constructor(
    private val userDao: UserDao,
    private val sportDao: SportDao,
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

    // FireSport methods
    fun getAllSports(): List<Sport> {
        return sportDao.getAll()
    }


    // Review methods
    private fun getAllReviewsByPlaygroundId(id: Int): List<Review> {
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

    fun getDetailedReservationById(id: Int): DetailedReservation {
        val reservation = reservationDao.findDetailedReservationById(id)
        val equipments =
            equipmentDao.findReservationEquipmentsByReservationId(id).toMutableList()
        if (!equipments.isNullOrEmpty()) {
            reservation.equipments = equipments
        }
        return reservation
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
                    reservation.id,
                    reservation.startTime,
                    reservation.endTime,
                    reservation.sportCenterId,
                    reservation.sportId
                )
            ) {
                // Delete equipments if any
                if (reservation.id != 0) {
                    equipmentDao.deleteEquipmentReservationByPlaygroundReservationId(reservation.id)
                    reservationDao.deleteById(reservation.id)
                }

                if (slotsAreAvailable(
                        reservation.playgroundId,
                        reservation.startTime,
                        reservation.endTime
                    )
                ) {
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
                } else {
                    return Pair(null, NewReservationError.EQUIPMENT_CONFLICT)
                }
            } else {
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
        reservationId: Int,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        sportCenterId: Int,
        sportId: Int
    ): Boolean {
        equipmentDao.findEquipmentReservationsForSpecifiedTimeInterval(
            reservationId,
            sportCenterId,
            sportId,
            startDateTime.toString(),
            endDateTime.toString()
        )
            .forEach { busyEquipment ->
                equipments.forEach { selectedEquipment ->
                    if (busyEquipment.equipmentId == selectedEquipment.equipmentId) {
                        if (busyEquipment.maxQuantity - busyEquipment.selectedQuantity < selectedEquipment.selectedQuantity) {
                            return false
                        }
                    }
                }
            }
        return true
    }

    private fun calculatePrice(reservation: NewReservation): Float {
        val duration = Duration.between(reservation.startTime, reservation.endTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() - hours * 60
        val pricePerHour =
            playgroundSportDao.getPlaygroundSportPricePerHour(reservation.playgroundId)
        var cost = (hours * pricePerHour) + (minutes * pricePerHour / 60)
        reservation.selectedEquipments.forEach {
            cost += it.selectedQuantity * equipmentDao.getEquipmentUnitPrice(it.equipmentId)
        }
        return cost


    }



    fun getReservationsPerDateByUserId(userId: Int): Map<LocalDate, List<DetailedReservation>> {
        val userReservations = reservationDao.findByUserId(userId)
        return userReservations.sortedBy { LocalDateTime.parse(it.startDateTime) }
            .groupBy { it.date }
    }


    // * Equipment methods *

    fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: Int,
        sportId: Int,
        reservationId: Int,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): MutableMap<Int, Equipment> {
        // retrieve equipment reservations happening in the specified time interval,
        // for the specified sport center and sport (and excluding the specified reservation ones)
        val equipmentReservations = equipmentDao.findEquipmentReservationsForSpecifiedTimeInterval(
            reservationId,
            sportCenterId,
            sportId,
            startDateTime.toString(),
            endDateTime.toString()
        )

        val startSlot = startDateTime
        val endSlot = endDateTime.minusMinutes(30)

        val equipmentsAvailableQuantitiesInSlots =
            equipmentReservations.asSequence().flatMap { equipmentReservation ->
                val reservationStartDateTime = equipmentReservation.startLocalDateTime
                val reservationEndDateTime = equipmentReservation.endLocalDateTime

                val equipmentReservationsPerSlot =
                    mutableListOf<Pair<LocalDateTime, EquipmentReservationForAvailabilities>>()
                var currentDateTime = reservationStartDateTime
                while (currentDateTime <= reservationEndDateTime) {
                    equipmentReservationsPerSlot.add(
                        Pair(
                            currentDateTime,
                            equipmentReservation.clone()
                        )
                    )
                    currentDateTime = currentDateTime.plusMinutes(30)
                }

                equipmentReservationsPerSlot
            }.groupBy { (slot, equipmentReservation) ->
                // create Equipment entity
                // create equals() and hashCode() for id Equipment only
                // pass it to the Pair constructor instead of equipment Id
                val equipment = Equipment(
                    equipmentReservation.equipmentId,
                    equipmentReservation.equipmentName,
                    equipmentReservation.sportId,
                    equipmentReservation.sportCenterId,
                    equipmentReservation.unitPrice,
                    equipmentReservation.maxQuantity
                )
                Pair(slot, equipment)
            }.mapValues { (_, pairList) ->
                val equipmentTotalSelectedQuantity =
                    pairList.sumOf { (_, equipmentReservation) -> equipmentReservation.selectedQuantity }
                val equipmentAvailability = pairList.first().second.maxQuantity

                Pair(equipmentTotalSelectedQuantity, equipmentAvailability)
            }.filter { (slotEquipmentPair, _) ->
                val slot = slotEquipmentPair.first

                slot >= startSlot && slot <= endSlot
            }.asSequence()
                .groupBy { (slotEquipmentPair, _) ->
                    val equipment = slotEquipmentPair.second
                    equipment
                }.mapValues { (_, equipmentQuantitiesPerSlotAndId) ->
                    val maxSelectedQuantityInSlots =
                        equipmentQuantitiesPerSlotAndId.maxOf { (_, equipmentQuantities) ->
                            val selectedQtyInSlots = equipmentQuantities.first
                            selectedQtyInSlots
                        }

                    val (_, maxAvailability) = equipmentQuantitiesPerSlotAndId.first().value

                    // compute available equipments left
                    val availableEquipmentsLeft = maxAvailability - maxSelectedQuantityInSlots

                    // equipmentQuantitiesPerSlotAndId.first().key.second.availability = availableEquipmentsLeft
                    // equipmentQuantitiesPerSlotAndId.first().key.second
                    availableEquipmentsLeft
                }

        // (they still miss the ones with full availability)
        val equipmentsWithCurrentAvailability =
            equipmentsAvailableQuantitiesInSlots.map { (equipment, availableQty) ->
                equipment.availability = availableQty
                equipment
        }.toMutableList()

        // take all equipments and add the ones with full availability
        val allEquipments = equipmentDao.findBySportCenterIdAndSportId(sportCenterId, sportId)

        for (virginEquipment in allEquipments) {
            if (!equipmentsWithCurrentAvailability.contains(virginEquipment)) {
                equipmentsWithCurrentAvailability.add(virginEquipment)
            }
        }

        // now equipmentsWithCurrentAvailability contains all the equipments (even with qty = 0),
        // with the actual availability

        return equipmentsWithCurrentAvailability
            .associateBy { equipment -> equipment.id }
            .toMutableMap()
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
        playgroundInfo.overallFacilitiesRating = overallFacilitiesRating.takeIf { !it.isNaN() } ?: 0f

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
                    it.sportEmoji,
                    it.sportName,
                    it.sportCenterId,
                    it.sportCenterName,
                    it.sportCenterAddress,
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
                    it.sportEmoji,
                    it.sportName,
                    it.sportCenterId,
                    it.sportCenterName,
                    it.sportCenterAddress,
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

    fun getAllPlaygroundsInfo(): List<PlaygroundInfo> {
        val allPlaygroundsInfo = playgroundSportDao.getAllPlaygroundInfo()

        allPlaygroundsInfo.forEach { playgroundInfo ->
            playgroundInfo.reviewList = getAllReviewsByPlaygroundId(playgroundInfo.playgroundId)

            val overallQualityRating: Float =
                playgroundInfo.reviewList.map { it.qualityRating }.filter { it != 0f }.average()
                    .toFloat()

            val overallFacilitiesRating: Float =
                playgroundInfo.reviewList.map { it.facilitiesRating }.filter { it != 0f }.average()
                    .toFloat()

            playgroundInfo.overallQualityRating = overallQualityRating.takeIf { !it.isNaN() } ?: 0f
            playgroundInfo.overallFacilitiesRating = overallFacilitiesRating.takeIf { !it.isNaN() } ?: 0f

            playgroundInfo.overallRating = when {
                playgroundInfo.overallQualityRating == 0f -> playgroundInfo.overallFacilitiesRating
                playgroundInfo.overallFacilitiesRating == 0f -> playgroundInfo.overallQualityRating
                else -> (playgroundInfo.overallQualityRating + playgroundInfo.overallFacilitiesRating) / 2
            }
        }

        return allPlaygroundsInfo
    }
}




