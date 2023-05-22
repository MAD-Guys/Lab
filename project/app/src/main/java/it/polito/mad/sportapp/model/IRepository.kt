package it.polito.mad.sportapp.model

import androidx.lifecycle.LiveData
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.firestore.FireResult
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

interface IRepository {

    // * User methods *
    fun getUser(id: Int): LiveData<FireResult<User>>
    fun userAlreadyExists(uid: String): LiveData<FireResult<Boolean>>
    fun usernameAlreadyExists(username: String): LiveData<FireResult<Boolean>>
    fun insertNewUser(user: User): LiveData<FireResult<Unit>>
    fun updateUser(user: User): LiveData<FireResult<Unit>>

    // * Sport methods *
    fun getAllSports(): LiveData<FireResult<List<Sport>>>

    // * Review methods *
    fun getReviewByUserIdAndPlaygroundId(userId: Int, playgroundId: Int): LiveData<FireResult<Review>>
    fun updateReview(review: Review): LiveData<FireResult<Unit>>
    fun deleteReview(review: Review): LiveData<FireResult<Unit>>

    // * Reservation methods *
    fun getDetailedReservationById(id: Int): LiveData<FireResult<DetailedReservation>>

    /**
     * Create a new reservation in the DB, or override the existing one if
     * any (with the same reservationId) Returns a Pair of (newReservationId,
     * error), where:
     * - 'newReservationId' is the new id assigned to the reservation (or the
     *   same as the previous one, if any), if the save succeeds; 'null'
     *   otherwise
     * - 'error' is an instance of NewReservationError reflecting the type of
     *   error occurred, or 'null' otherwise
     */
    fun overrideNewReservation(reservation: NewReservation): LiveData<FireResult<Pair<Int?, NewReservationError?>>>
    fun getReservationsPerDateByUserId(uid: String): LiveData<FireResult<Map<LocalDate, List<DetailedReservation>>>>
    fun addUserToReservation(reservationId: Int, uid: String): LiveData<FireResult<Unit>>

    // * Equipment methods *
    fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: Int,
        sportId: Int,
        reservationId: Int,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): LiveData<FireResult<MutableMap<Int, Equipment>>>

    fun deleteReservation(reservation: DetailedReservation): LiveData<FireResult<Unit>>

    // * Playground methods *
    fun getPlaygroundInfoById(playgroundId: Int): LiveData<FireResult<PlaygroundInfo>>
    fun getAvailablePlaygroundsPerSlot(month: YearMonth, sport: Sport?): LiveData<FireResult<
            MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>>>>

    fun getAllPlaygroundsInfo(): LiveData<FireResult<List<PlaygroundInfo>>>

    // * Notification methods *
    fun getNotificationsByUserId(uid: String): LiveData<FireResult<MutableList<Notification>>>
    fun deleteNotification(notificationId: Int): LiveData<FireResult<Unit>>

    // * enums *

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

}