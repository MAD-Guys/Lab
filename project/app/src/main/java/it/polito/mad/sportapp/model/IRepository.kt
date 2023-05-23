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
import it.polito.mad.sportapp.entities.firestore.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.FireErrorType
import it.polito.mad.sportapp.entities.firestore.FireResult
import it.polito.mad.sportapp.entities.firestore.GetItemFireError
import it.polito.mad.sportapp.entities.firestore.InsertItemFireError
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.security.auth.callback.Callback

interface IRepository {

    // * User methods *
    fun getUser(uid: String, fireCallback: (FireResult<User, GetItemFireError>) -> Unit)
    fun userAlreadyExists(uid: String, fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit)
    fun usernameAlreadyExists(username: String, fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit)
    fun insertNewUser(user: User, fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit)
    fun updateUser(user: User, fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit)

    // * Sport methods *
    fun getAllSports(fireCallback: (FireResult<List<Sport>, DefaultFireError>) -> Unit)

    // * Review methods *
    fun getReviewByUserIdAndPlaygroundId(
        uid: String,
        playgroundId: String,
        fireCallback: (FireResult<Review, GetItemFireError>) -> Unit
    )

    fun updateReview(review: Review, fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit)
    fun deleteReview(review: Review, fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit)

    // * Reservation methods *
    fun getDetailedReservationById(
        reservationId: String,
        fireCallback: (FireResult<DetailedReservation, GetItemFireError>) -> Unit
    )

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
    fun overrideNewReservation(
        reservation: NewReservation,
        // * custom error type *
        fireCallback: (FireResult<Int, NewReservationError>) -> Unit
    )

    fun getReservationsPerDateByUserId(
        uid: String,
        fireCallback: (FireResult<Map<LocalDate, List<DetailedReservation>>, GetItemFireError>) -> Unit
    )

    fun addUserToReservation(
        reservationId: String,
        uid: String,
        fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit
    )

    // * Equipment methods *
    fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: String,
        sportId: String,
        reservationId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        fireCallback: (FireResult<MutableMap<Int, Equipment>, DefaultFireError>) -> Unit
    )

    fun deleteReservation(
        reservation: DetailedReservation,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    )

    // * Playground methods *
    fun getPlaygroundInfoById(
        playgroundId: String,
        fireCallback: (FireResult<PlaygroundInfo, GetItemFireError>) -> Unit
    )

    fun getAvailablePlaygroundsPerSlot(
        month: YearMonth, sport: Sport?, fireCallback: (
            FireResult<
                    MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>>,
                    DefaultFireError>
        ) -> Unit
    )

    fun getAllPlaygroundsInfo(fireCallback: (FireResult<List<PlaygroundInfo>, DefaultFireError>) -> Unit)

    // * Notification methods *
    fun getNotificationsByUserId(uid: String, fireCallback: (FireResult<MutableList<Notification>, DefaultFireError>) -> Unit)
    fun deleteNotification(notificationId: String, fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit)

    // * enums *

    enum class NewReservationError(val message: String) : FireErrorType{
        SLOT_CONFLICT(
            "Ouch! the selected slots have just been booked by someone else \uD83D\uDE41. Please select new ones for your reservation!"
        ),
        EQUIPMENT_CONFLICT(
            "Ouch! the selected equipments have just been booked by someone else \uD83D\uDE41. Please select new ones for your reservation!"
        ),
        UNEXPECTED_ERROR(
            "An unexpected error occurred while saving your reservation. Please try again or check your connection status."
        );

        override fun message(): String {
            return message
        }
    }
}