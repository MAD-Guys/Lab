package it.polito.mad.sportapp.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.sportapp.entities.Achievement
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.FireSport
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult.*
import it.polito.mad.sportapp.entities.firestore.FireUser
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.GetItemFireError
import it.polito.mad.sportapp.entities.firestore.utilities.InsertItemFireError
import it.polito.mad.sportapp.entities.firestore.utilities.NewReservationError
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class FireRepository : IRepository {
    private val db = FirebaseFirestore.getInstance()

    /* users */

    /**
     * This method gets the user given its uid
     * Note: the result is *dynamic*, i.e. the fireCallback gets called each time the user changes
     * (but the achievements are static)
     */
    override fun getUser(
        userId: String,
        fireCallback: (FireResult<User, GetItemFireError>) -> Unit
    ): FireListener {
        val fireListener = FireListener()

        val userListener = db.collection("users")
            .document(userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("default error", "Error: a generic error occurred retrieving user with id $userId in FireRepository.getUser(). Message: ${error.message}")

                    fireCallback(
                        GetItemFireError.default(
                            "Error: a generic error occurred retrieving user with id $userId",
                        )
                    )

                    return@addSnapshotListener
                }

                if (value == null || !value.exists()) {
                    // no data exists
                    fireCallback(
                        GetItemFireError.notFound(
                        "Error: User with id $userId has not been found in FireRepository.getUser()"
                    ))
                    return@addSnapshotListener
                }

                // * user exists *

                // deserialize data from db
                val fireUser = FireUser.deserialize(value.id, value.data)

                if (fireUser == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: a generic error occurred deserializing user with id $userId in FireRepository.getUser()")

                    fireCallback(GetItemFireError.duringDeserialization(
                        "Error: a generic error occurred deserializing user with id $userId"
                    ))
                    return@addSnapshotListener
                }

                // * user correctly retrieved *

                // transform to user entity
                val user = fireUser.toUser()

                // compute user achievements (statically)
                this.buildAchievements(userId) { result ->
                    when (result) {
                        is Success -> {
                            // attach user achievements and return successfully
                            user.achievements = result.unwrap()
                            fireCallback(Success(user))
                        }
                        is Error -> {
                            fireCallback(Error(result.errorType()))
                        }
                    }
                }
            }

        // track listener that will have to be unregistered
        fireListener.add(userListener)

        return fireListener
    }

    private fun buildAchievements(
        userId: String,
        fireCallback: (FireResult<Map<Achievement, Boolean>, GetItemFireError>) -> Unit
    ) {
        // TODO: statically compute the real achievements of user $userId
        fireCallback(Success(
            mapOf(
                Achievement.AtLeastOneSport to false,
                Achievement.AtLeastFiveSports to false,
                Achievement.AllSports to false,
                Achievement.AtLeastThreeMatches to false,
                Achievement.AtLeastTenMatches to false,
                Achievement.AtLeastTwentyFiveMatches to false,
            )
        ))
    }

    /**
     * Check if the user already exists or not
     * Note: the result is retrieved as static (fireCallback is executed just once)
     */
    override fun userAlreadyExists(
        userId: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userExists = document.exists()

                fireCallback(Success(userExists))
                return@addOnSuccessListener
            }
            .addOnFailureListener {
                Log.d("default error","Error: a generic error occurred while checking user with id $userId in FireRepository.userAlreadyExists(). Message: ${it.message}")

                fireCallback(DefaultFireError.withMessage(
                    "Error: a generic error occurred while checking user with id $userId")
                )
            }
    }

    /**
     * Check if the username already exists or not
     * Note: the result is retrieved as static (fireCallback is executed just once)
     */
    override fun usernameAlreadyExists(
        username: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                val userNameAlreadyExists = !documents.isEmpty

                fireCallback(Success(userNameAlreadyExists))
            }
            .addOnFailureListener {
                Log.d("default error", "Error: a generic error occurred while checking username $username " +
                        "existence in FireRepository.usernameAlreadyExists(). Message: ${it.message}")

                fireCallback(DefaultFireError.withMessage(
                    "Error: a generic error occurred while checking username $username"
                ))
            }
    }

    /**
     * Insert a new user in the cloud Firestore db inside users collection
     */
    override fun insertNewUser(
        user: User,
        fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit
    ) {
        // convert User entity into FireUser
        val fireUser = FireUser.from(user)

        // serialize user
        val serializedUser = fireUser.serialize()

        db.collection("users")
            .document()
            .set(serializedUser)
            .addOnSuccessListener {
                fireCallback(Success(Unit))
            }
            .addOnFailureListener {
                Log.d("default error", "Error: a generic error occurred inserting new user $user in FireRepository.insertNewUser(). Message: ${it.message}")
                fireCallback(InsertItemFireError.default(
                    "Error: a generic error occurred inserting new user $user"
                ))
            }
    }

    /**
     * Update an existing user
     */
    override fun updateUser(
        user: User,
        fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit
    ) {
        // convert User entity into FireUser
        val fireUser = FireUser.from(user)

        if(fireUser.id == null) {
            // serialization error: cannot update a user with id null
            Log.d("serialization error", "Error: tyring to update user with id null ($user) in FireRepository.updateUser()")
            fireCallback(
                InsertItemFireError.duringSerialization(
                    "Error: trying to update user with id null"
                )
            )
            return
        }

        // * user id is not null *

        // serialize it
        val serializedUser = fireUser.serialize()

        db.collection("users")
            .document(fireUser.id)
            .set(serializedUser)
            .addOnSuccessListener {
                fireCallback(Success(Unit))
            }
            .addOnFailureListener {
                // default error occurred
                Log.d("default error", "Error: a generic error occurred updating user $user in FireRepository.updateUser(). Message: ${it.message}")
                fireCallback(InsertItemFireError.default(
                    "Error: a generic error occurred updating user $user"
                ))
            }
    }

    override fun getAllUsers(fireCallback: (FireResult<List<User>, GetItemFireError>) -> Unit) {
        TODO("Not yet implemented")
    }

    /* sports */

    /**
     * Retrieve all the sports
     * Note: the result is retrieved as **static** (fireCallback is executed just once)
     */
    override fun getAllSports(fireCallback: (FireResult<List<Sport>, GetItemFireError>) -> Unit) {
        db.collection("sports")
            .get()
            .addOnSuccessListener { result ->
                if(result == null) {
                    Log.d("generic error", "Error: a generic error occurred retrieving all Sports in FireRepository.getAllSports()")
                    fireCallback(GetItemFireError.default(
                        "Error: a generic error occurred retrieving all Sports"
                    ))
                    return@addOnSuccessListener
                }

                val allSports = mutableListOf<Sport>()

                for (document in result) {
                    val deserializedSport = FireSport.deserialize(document.id, document.data)

                    if (deserializedSport == null) {
                        // deserialization error
                        Log.d("deserialization error", "Error: an error occurred deserializing sport document with id ${document.id} in FireRepository.getAllSports()")
                        fireCallback(
                            GetItemFireError.duringDeserialization(
                                "Error: an error occurred deserializing sport document with id ${document.id}"
                            )
                        )
                        return@addOnSuccessListener
                    }

                    // convert to entity
                    val sport = deserializedSport.toSport()
                    allSports.add(sport)
                }

                // return successfully
                fireCallback(Success(allSports))
            }
            .addOnFailureListener {
                Log.d("generic error", "Error: a generic error occurred retrieving all Sports in FireRepository.getAllSports(). Message: ${it.message}")
                fireCallback(GetItemFireError.default(
                    "Error: a generic error occurred retrieving all Sports"
                ))
            }
    }

    /* reviews */

    override fun getReviewByUserIdAndPlaygroundId(
        uid: String,
        playgroundId: String,
        fireCallback: (FireResult<Review, GetItemFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    override fun updateReview(
        review: Review,
        fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteReview(
        review: Review,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    /* reservations */

    override fun getDetailedReservationById(
        reservationId: String,
        fireCallback: (FireResult<DetailedReservation, GetItemFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    override fun overrideNewReservation(
        reservation: NewReservation,
        fireCallback: (FireResult<Int, NewReservationError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getReservationsPerDateByUserId(
        uid: String,
        fireCallback: (FireResult<Map<LocalDate, List<DetailedReservation>>, GetItemFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    override fun addUserToReservation(
        reservationId: String,
        uid: String,
        fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: String,
        sportId: String,
        reservationId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        fireCallback: (FireResult<MutableMap<Int, Equipment>, DefaultFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    override fun deleteReservation(
        reservation: DetailedReservation,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    /* playgrounds */

    override fun getPlaygroundInfoById(
        playgroundId: String,
        fireCallback: (FireResult<PlaygroundInfo, GetItemFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    override fun getAvailablePlaygroundsPerSlot(
        month: YearMonth,
        sport: Sport?,
        fireCallback: (FireResult<MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>>, DefaultFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    override fun getAllPlaygroundsInfo(fireCallback: (FireResult<List<PlaygroundInfo>, DefaultFireError>) -> Unit): FireListener {
        TODO("Not yet implemented")
    }

    override fun loggedUserCanReviewPlayground(
        uid: String,
        playgroundId: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
        /**
         * This method returns a fireResult "true" if the logged user can review the given playground.
         * The condition is that the user should have played there almost once in the past, in
         * other words: exists a reservation created by the user, or with the user among the participants,
         * which start date is before the current date.
         * If it is not verified, the method returns a fireResult "false", or a DefaultFireError only
         * in case of firebase server error.
         * */
    }

    /* notifications */

    override fun getNotificationsByUserId(
        userId: String,
        fireCallback: (FireResult<MutableList<Notification>, DefaultFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    override fun deleteNotification(
        notificationId: String,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}