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
import it.polito.mad.sportapp.entities.NotificationStatus
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultInsertFireError
import it.polito.mad.sportapp.entities.firestore.utilities.NewReservationError
import it.polito.mad.sportapp.entities.firestore.utilities.SaveAndSendInvitationFireError
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

interface IRepository {

    // * User methods *

    /**
     * This method gets the user with its achievements given its uid
     *
     * **Note**: the result is *dynamic*,
     * i.e. the fireCallback gets called each time the user changes (but the
     * achievements are static)
     */
    fun getUserWithAchievements(
        userId: String,
        fireCallback: (FireResult<User, DefaultGetFireError>) -> Unit
    ): FireListener

    /**
     * Retrieve the user given its id from the db
     *
     * **Note**: the result is *static* (i.e. the fireCallback gets called just once)
     */
    fun getStaticUser(
        userId: String,
        fireCallback: (FireResult<User, DefaultGetFireError>) -> Unit
    )

    /** Check if the user already exists or not */
    fun userAlreadyExists(
        userId: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    )

    /**
     * Check if a given username already exists or not
     *
     * **Note**: the result is retrieved as **static** (fireCallback is executed just once)
     */
    fun usernameAlreadyExists(
        username: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    )

    /** Insert a new user in the db */
    fun insertNewUser(user: User, fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit)

    /** Update an existing user */
    fun updateUser(user: User, fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit)

    /** Update the notifications token used by the user */
    fun updateUserToken(
        userId: String,
        newToken: String,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    )

    /** Update the profile url of the user */
    fun updateUserImageUrl(
        userId: String,
        newImageUrl: String,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    )

    /**
     * Retrieve all users from db which the specified user can still send the
     * notification to, for the specified reservation
     *
     * **Note**: the result is **dynamic** (fireCallback is executed each time the list changes)
     */
    fun getAllUsersToSendInvitationTo(
        senderId: String,
        reservationId: String,
        fireCallback: (FireResult<List<User>, DefaultGetFireError>) -> Unit
    ): FireListener

    // * ProfileSport methods *

    /**
     * Retrieve all the sports
     *
     * **Note**: the result is retrieved as **static** (fireCallback is executed just once)
     */
    fun getAllSports(fireCallback: (FireResult<List<Sport>, DefaultGetFireError>) -> Unit)


    // * Review methods *

    /**
     * Retrieve a specific Review entity from the db, providing the related
     * playground id and user id
     *
     * **Note**: the result is **dynamic** (fireCallback is executed each time the Review is updated);
     * remember to **unregister** the returned listener once you don't need it anymore
     */
    fun getReviewByUserIdAndPlaygroundId(
        userId: String,
        playgroundId: String,
        fireCallback: (FireResult<Review, DefaultGetFireError>) -> Unit
    ): FireListener

    /** Create or update an existing Review in the db */
    fun insertOrUpdateReview(
        review: Review,
        fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit
    )

    /** Delete an existing Review from the db */
    fun deleteReview(review: Review, fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit)

    /**
     * This method returns a fireResult "true" if the logged user can review
     * the given playground; "false" otherwise. The condition is that the user
     * should have played there almost once in the past, in other words: if
     * exists a reservation involving the user as a participant, whose endTime
     * is before the current time, it returns a fireResult "true". It returns a
     * DefaultFireError in case of an error.
     */
    fun loggedUserCanReviewPlayground(
        userId: String,
        playgroundId: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    )

    // * Reservation methods *

    /**
     * Retrieve a specific (detailed) reservation from the db, given its id
     *
     * **Note**: the result is **dynamic** (fireCallback is called each time
     * the reservation changes) Remember to unregister the listener once you
     * don't need the reservation anymore
     */
    fun getDetailedReservationById(
        reservationId: String,
        fireCallback: (FireResult<DetailedReservation, DefaultGetFireError>) -> Unit
    ): FireListener

    /**
     * Create a new reservation in the DB, or override the existing one
     * if any (with the same reservationId) Returns a Success with the
     * newReservationId, if the save went well, or an Error if an error
     * occurred
     * - 'newReservationId' is the new id assigned to the reservation (or the
     *   same as the previous one, if any)
     * - 'error' is an instance of NewReservationError reflecting the type of
     *   error occurred
     */
    fun overrideNewReservation(
        userId: String,
        reservation: NewReservation,
        // * custom error type *
        fireCallback: (FireResult<String, NewReservationError>) -> Unit
    )

    /**
     * Retrieve from the db all the reservations in which the user is involved
     * as a participant
     */
    fun getReservationsPerDateByUserId(
        userId: String,
        fireCallback: (FireResult<Map<LocalDate, List<DetailedReservation>>, DefaultGetFireError>) -> Unit
    ): FireListener

    /**
     * Retrieve from the db the additional requests of the given reservation (if any)
     *
     * **Note**: the result is static (the fireCallback is executed just once)
     */
    fun getReservationAdditionalRequests(
        reservationId: String,
        fireCallback: (FireResult<String?,DefaultGetFireError>) -> Unit
    )

    /** Delete a reservation from the db */
    fun deleteReservation(
        reservation: DetailedReservation,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    )

    // * Equipment methods *

    fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: String,
        sportId: String,
        reservationId: String?,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        fireCallback: (FireResult<MutableMap<String, Equipment>, DefaultGetFireError>) -> Unit
    ): FireListener

    /**
     * Retrieve from the db all the equipments related to a given sport,
     * available in the specified sport center
     */
    fun getAllEquipmentsBySportCenterIdAndSportId(
        sportCenterId: String,
        sportId: String,
        fireCallback: (FireResult<MutableList<Equipment>, DefaultGetFireError>) -> Unit
    ): FireListener

    // * Playground methods *

    /** Retrieve the specified Playground info entity from the db */
    fun getPlaygroundInfoById(
        playgroundId: String,
        fireCallback: (FireResult<PlaygroundInfo, DefaultGetFireError>) -> Unit
    ): FireListener

    /**
     * Retrieve from the db all the available playgrounds, for a given sport,
     * in each slot of a given month
     */
    fun getAvailablePlaygroundsPerSlot(
        month: YearMonth, sport: Sport?, fireCallback: (
            FireResult<
                    MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>>?,
                    DefaultGetFireError>
        ) -> Unit
    ): FireListener

    /**
     * Retrieve all the Playground Info entities from the db
     *
     * **Note**: the result is **static** (the fireCallback is executed just once)
     */
    fun getAllPlaygroundsInfo(
        fireCallback: (FireResult<List<PlaygroundInfo>, DefaultGetFireError>) -> Unit
    )

    // * Notification methods *

    /**
     * Return all the notifications related to future reservations for a given user
     *
     * **Note**: the result is **dynamic** (the fireCallback is called
     * every time a new notification is received)
     */
    fun getNotificationsByUserId(
        userId: String,
        fireCallback: (FireResult<MutableList<Notification>, DefaultGetFireError>) -> Unit
    ): FireListener

    /**
     * Update invitation status and corresponding reservation participants,
     * based on the old and the new invitation status:
     * - if newStatus is ACCEPTED -> update notification status and **insert**
     *   new user as a reservation's participant (in this case, oldStatus is
     *   always PENDING, since user cannot accept invitation after a refuse)
     * - if newStatus is REJECTED and oldStatus is PENDING -> just update
     *   notification status (user is answering for the first time)
     * - if newStatus is REJECTED and oldStatus is ACCEPTED -> update
     *   notification status and **remove** user from reservation's
     *   participants
     */
    fun updateInvitationStatus(
        notificationId: String,
        oldStatus: NotificationStatus,
        newStatus: NotificationStatus,
        reservationId: String,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    )

    /**
     * (1) Save a new invitation to the db and (2) send the corresponding push
     * notification to the receiver
     */
    fun saveAndSendInvitation(
        notification: Notification,
        fireCallback: (FireResult<Unit, SaveAndSendInvitationFireError>) -> Unit
    )

    /**
     * Retrieve a notification given its id from the db
     *
     * **Note**: the result is **dynamic** (the fireCallback is executed each time the notification
     * changes). Remember to unregister the listener once you don't need the data anymore
     */
    fun getNotificationById(
        notificationId: String,
        fireCallback: (FireResult<Notification, DefaultGetFireError>) -> Unit
    ): FireListener
}