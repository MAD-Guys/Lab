package it.polito.mad.sportapp.model

import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.User
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

interface IRepository
{
    // * User methods *
    fun getUser(id: Int): User
    fun usernameAlreadyExists(username: String): Boolean
    fun updateUser(user: User)

    // * Sport methods *
    fun getAllSports(): List<Sport>

    // * Review methods *
    fun getReviewByUserIdAndPlaygroundId(userId: Int, playgroundId: Int): Review
    fun updateReview(review: Review)
    fun deleteReview(review: Review)

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

    // * Reservation methods *
    fun getDetailedReservationById(id: Int): DetailedReservation
    /**
     * Create a new reservation in the DB, or override the existing one if any (with the same reservationId)
     * Returns a Pair of (newReservationId, error), where:
     * - 'newReservationId' is the new id assigned to the reservation
     *  (or the same as the previous one, if any), if the save succeeds; 'null' otherwise
     * - 'error' is an instance of NewReservationError reflecting the type of error occurred, or 'null' otherwise
     */
    fun overrideNewReservation(reservation: NewReservation): Pair<Int?, NewReservationError?>
    fun getReservationsPerDateByUserId(userId: Int): Map<LocalDate, List<DetailedReservation>>

    // * Equipment methods *
    fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: Int,
        sportId: Int,
        reservationId: Int,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): MutableMap<Int, Equipment>
    fun deleteReservation(reservation: DetailedReservation)

    // * Playground methods *
    fun getPlaygroundInfoById(playgroundId: Int): PlaygroundInfo
    fun getAvailablePlaygroundsPerSlot(month: YearMonth, sport: Sport?)
            : MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>>
    fun getAllPlaygroundsInfo(): List<PlaygroundInfo>
}