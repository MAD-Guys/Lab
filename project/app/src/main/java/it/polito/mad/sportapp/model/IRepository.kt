package it.polito.mad.sportapp.model

import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultInsertFireError
import it.polito.mad.sportapp.entities.firestore.utilities.NewReservationError
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

interface IRepository {

    // * User methods *

    /**
     * This method gets the user given its uid
     * **Note**: the result is **dynamic**: the fireCallback gets called each time the user changes.
     * Remember to **unregister** the listener once you don't need it anymore
     */
    fun getUser(userId: String, fireCallback: (FireResult<User, DefaultGetFireError>) -> Unit) : FireListener

    /**
     * Check if the user already exists or not
     */
    fun userAlreadyExists(userId: String, fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit)

    /**
     * Check if a given username already exists or not
     * Note: the result is retrieved as **static** (fireCallback is executed just once)
     */
    fun usernameAlreadyExists(username: String, fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit)

    /**
     * Insert a new user in the db
     */
    fun insertNewUser(user: User, fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit)

    /**
     * Update an existing user
     */
    fun updateUser(user: User, fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit)


    fun getAllUsers(fireCallback: (FireResult<List<User>, DefaultGetFireError>) -> Unit)

    // * Sport methods *

    /**
     * Retrieve all the sports
     * Note: the result is retrieved as **static** (fireCallback is executed just once)
     */
    fun getAllSports(fireCallback: (FireResult<List<Sport>, DefaultGetFireError>) -> Unit)


    // * Review methods *

    /**
     * Retrieve a specific Review entity from the db,
     * providing the related playground id and user id
     * **Note**: the result is **dynamic** (fireCallback is executed each time the Review is updated);
     * remember to **unregister** the returned listener once you don't need it anymore
     */
    fun getReviewByUserIdAndPlaygroundId(
        userId: String,
        playgroundId: String,
        fireCallback: (FireResult<Review, DefaultGetFireError>) -> Unit
    ) : FireListener

    /**
     * Create or update an existing Review in the db
     */
    fun insertOrUpdateReview(review: Review, fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit)

    /**
     * Delete an existing Review in the db
     */
    fun deleteReview(review: Review, fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit)


    // * Reservation methods *

    fun getDetailedReservationById(
        reservationId: String,
        fireCallback: (FireResult<DetailedReservation, DefaultGetFireError>) -> Unit
    ) : FireListener

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
        fireCallback: (FireResult<Map<LocalDate, List<DetailedReservation>>, DefaultGetFireError>) -> Unit
    ) : FireListener

    fun addUserToReservation(
        reservationId: String,
        uid: String,
        fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit
    )

    // * Equipment methods *
    fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: String,
        sportId: String,
        reservationId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        fireCallback: (FireResult<MutableMap<Int, Equipment>, DefaultFireError>) -> Unit
    ) : FireListener

    fun getAllEquipmentsBySportCenterIdAndSportId(
        sportCenterId: String,
        sportId: String,
        fireCallback: (FireResult<MutableMap<Int, Equipment>, DefaultFireError>) -> Unit
    ) : FireListener

    fun deleteReservation(
        reservation: DetailedReservation,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    )

    // * Playground methods *
    fun getPlaygroundInfoById(
        playgroundId: String,
        fireCallback: (FireResult<PlaygroundInfo, DefaultGetFireError>) -> Unit
    ) : FireListener

    fun getAvailablePlaygroundsPerSlot(
        month: YearMonth, sport: Sport?, fireCallback: (
            FireResult<
                    MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>>,
                    DefaultFireError>
        ) -> Unit
    ) : FireListener

    fun getAllPlaygroundsInfo(fireCallback: (FireResult<List<PlaygroundInfo>, DefaultFireError>) -> Unit): FireListener

    /**
     * This method returns a fireResult "true" if the logged user can review the given playground.
     * The condition is that the user should have played there almost once in the past, in
     * other words: exists a reservation created by the user, or with the user among the participants,
     * which start date is before the current date.
     * If it is not verified, the method returns a fireResult "false", or a DefaultFireError only
     * in case of firebase server error.
    * */
    fun loggedUserCanReviewPlayground(uid: String, playgroundId: String, fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit)

    // * Notification methods *
    fun getNotificationsByUserId(
        userId: String,
        fireCallback: (FireResult<MutableList<Notification>, DefaultFireError>) -> Unit
    ): FireListener

    fun deleteNotification(
        notificationId: String,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    )
}