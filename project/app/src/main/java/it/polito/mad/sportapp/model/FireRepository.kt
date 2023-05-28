package it.polito.mad.sportapp.model

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.NotificationStatus
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.FireEquipmentReservationSlot
import it.polito.mad.sportapp.entities.firestore.FireNotification
import it.polito.mad.sportapp.entities.firestore.FirePlaygroundReservation
import it.polito.mad.sportapp.entities.firestore.FirePlaygroundSport
import it.polito.mad.sportapp.entities.firestore.FireReview
import it.polito.mad.sportapp.entities.firestore.FireSport
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult.*
import it.polito.mad.sportapp.entities.firestore.FireUser
import it.polito.mad.sportapp.entities.firestore.FireUserForPlaygroundReservation
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultInsertFireError
import it.polito.mad.sportapp.entities.firestore.utilities.NewReservationError
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class FireRepository : IRepository {
    internal val db = FirebaseFirestore.getInstance()

    /* users */

    // TODO
    /**
     * This method gets the user given its uid
     * Note: the result is *dynamic*, i.e. the fireCallback gets called each time the user changes
     * (but the achievements are static)
     */
    override fun getUser(
        userId: String,
        fireCallback: (FireResult<User, DefaultGetFireError>) -> Unit
    ): FireListener {
        val fireListener = FireListener()

        val userListener = db.collection("users")
            .document(userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d(
                        "default error",
                        "Error: a generic error occurred retrieving user with id $userId in FireRepository.getUser(). Message: ${error.message}"
                    )

                    fireCallback(
                        DefaultGetFireError.default(
                            "Error: a generic error occurred retrieving user",
                        )
                    )

                    return@addSnapshotListener
                }

                if (value == null || !value.exists()) {
                    // no data exists
                    fireCallback(
                        DefaultGetFireError.notFound(
                            "Error: User has not been found"
                        )
                    )
                    return@addSnapshotListener
                }

                // * user exists *

                // deserialize data from db
                val fireUser = FireUser.deserialize(value.id, value.data)

                if (fireUser == null) {
                    // deserialization error
                    Log.d(
                        "deserialization error",
                        "Error: a generic error occurred deserializing user with id $userId in FireRepository.getUser()"
                    )

                    fireCallback(
                        DefaultGetFireError.duringDeserialization(
                            "Error: a generic error occurred retrieving user"
                        )
                    )
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
                Log.d(
                    "default error",
                    "Error: a generic error occurred while checking user with id $userId in FireRepository.userAlreadyExists(). Message: ${it.message}"
                )

                fireCallback(
                    DefaultFireError.withMessage(
                        "Error: a generic error occurred while checking user"
                    )
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
        fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit
    ) {
        // convert User entity into FireUser
        val fireUser = FireUser.from(user)

        // serialize user
        val serializedUser = fireUser.serialize()

        // * No check about username uniqueness *

        db.collection("users")
            .document()
            .set(serializedUser)
            .addOnSuccessListener {
                fireCallback(Success(Unit))
            }
            .addOnFailureListener {
                Log.d("default error", "Error: a generic error occurred inserting new user $user in FireRepository.insertNewUser(). Message: ${it.message}")
                fireCallback(DefaultInsertFireError.default(
                    "Error: a generic error occurred inserting new user"
                ))
            }
    }

    // TODO
    /**
     * Update an existing user
     */
    override fun updateUser(
        user: User,
        fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit
    ) {
        // convert User entity into FireUser
        val fireUser = FireUser.from(user)

        if(fireUser.id == null) {
            // serialization error: cannot update a user with id null
            Log.d("serialization error", "Error: tyring to update user with id null ($user) in FireRepository.updateUser()")
            fireCallback(
                DefaultInsertFireError.duringSerialization(
                    "Error: an error occurred updating user"
                )
            )
            return
        }

        // * user id is not null *

        // serialize it
        val serializedUser = fireUser.serialize()

        // TODO: replace with a transaction to first set the user in the Users collection and then
        //  update username in PlaygroundReservations and Reviews collections too

        // * No check about username uniqueness *

        db.collection("users")
            .document(fireUser.id)
            .set(serializedUser)
            .addOnSuccessListener {
                fireCallback(Success(Unit))
            }
            .addOnFailureListener {
                // default error occurred
                Log.d("default error", "Error: a generic error occurred updating user $user in FireRepository.updateUser(). Message: ${it.message}")
                fireCallback(DefaultInsertFireError.default(
                    "Error: a generic error occurred updating user $user"
                ))
            }
    }

    /**
     * Update the notifications token used by the user
     */
    override fun updateUserToken(
        userId: String,
        newToken: String,
        fireCallback: (FireResult<Unit,DefaultFireError>) -> Unit
    ) {
        TODO()
    }

    /**
     * Retrieve all users from Firestore cloud db which the specified user
     * can still send the notification to, for the specified reservation
     * **Note**: the result is **dynamic** (fireCallback is executed each time the list changes)
     */
    override fun getAllUsersToSendInvitationTo(
        senderId: String,
        reservationId: String,
        fireCallback: (FireResult<List<User>, DefaultGetFireError>) -> Unit
    ): FireListener {
        val fireListener = FireListener()

        // first retrieve all users statically
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                if (documents == null) {
                    // generic error
                    Log.d("generic error", "Error: retrieved null users in FireRepository.getAllUsersToSendNotificationsTo($senderId, $reservationId)")
                    fireCallback(DefaultGetFireError.notFound(
                        "Error: users not found"
                    ))
                    return@addOnSuccessListener
                }

                val allUsers = mutableListOf<User>()

                for (document in documents) {
                    val deserializedUser = FireUser.deserialize(document.id, document.data)

                    if(deserializedUser == null) {
                        // deserialization error
                        Log.d("deserialization error", "Error: deserialization error in FireRepository.getAllUsersToSendNotificationsTo($senderId, $reservationId)")
                        fireCallback(DefaultGetFireError.duringDeserialization(
                            "Error: an error occurred retrieving users"
                        ))
                        return@addOnSuccessListener
                    }

                    // filter the sender
                    if(deserializedUser.id!! == senderId)
                        continue

                    // convert to entity
                    val user = deserializedUser.toUser()
                    allUsers.add(user)
                }

                // * retrieved all users *
                // now retrieve and exclude the ones to which a notification has already been sent

                // *dynamic result*
                val listener = db.collection("notifications")
                    .whereEqualTo("type", 0)    // INVITATION
                    .whereEqualTo("reservationId", reservationId)
                    .addSnapshotListener { value, error ->
                        if (error != null || value == null) {
                            // generic error
                            Log.d("generic error", "Error: a generic error occurred retrieving users to which an invitation has already been sent, in FireRepository.getAllUsersToSendNotificationsTo(). Message: ${error?.message}")
                            fireCallback(DefaultGetFireError.default(
                                "Error: a generic error occurred retrieving users"
                            ))
                            return@addSnapshotListener
                        }

                        // * users to exclude correctly retrieved *

                        val usersIdToExclude = mutableListOf<String>()

                        for (notificationDoc in value.documents) {
                            val fireNotification = FireNotification.deserialize(notificationDoc.id, notificationDoc.data)

                            if (fireNotification == null) {
                                // deserialization error
                                Log.d("deserialization error", "Error: an error occurred deserializing a notification with id ${notificationDoc.id} in FireRepository.getAllUsersToSendInvitationTo()")
                                fireCallback(DefaultGetFireError.duringDeserialization(
                                    "Error: a generic error occurred retrieving users"
                                ))
                                return@addSnapshotListener
                            }

                            // add receiver id to the users to exclude
                            usersIdToExclude.add(fireNotification.receiverId)
                        }

                        // copy users list
                        val usersToInvite = mutableListOf<User>()

                        allUsers.forEach { user ->
                            usersToInvite.add(user.clone())
                        }

                        // remove the users which have already received the invitation
                        usersToInvite.removeIf { user -> usersIdToExclude.contains(user.id!!) }

                        // return users successfully
                        fireCallback(Success(usersToInvite))
                    }

                fireListener.add(listener)
            }
            .addOnFailureListener {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving users in FireRepository.getAllUsersToSendNotificationsTo(). Message: ${it.message}")
                fireCallback(DefaultGetFireError.default(
                    "Error: a generic error occurred retrieving users"
                ))
            }

        return fireListener
    }

    /* sports */

    /**
     * Retrieve all the sports
     * Note: the result is retrieved as **static** (fireCallback is executed just once)
     */
    override fun getAllSports(fireCallback: (FireResult<List<Sport>, DefaultGetFireError>) -> Unit) {
        db.collection("sports")
            .get()
            .addOnSuccessListener { result ->
                if(result == null) {
                    Log.d("generic error", "Error: a generic error occurred retrieving all Sports in FireRepository.getAllSports()")
                    fireCallback(DefaultGetFireError.default(
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
                            DefaultGetFireError.duringDeserialization(
                                "Error: an error occurred retrieving the sport"
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
                fireCallback(DefaultGetFireError.default(
                    "Error: a generic error occurred retrieving all Sports"
                ))
            }
    }

    /* reviews */

    /**
     * Retrieve a specific review from the Firestore cloud db, given the user id and playground id
     * **Note**: the result is **dynamic** (the fireCallback is called each time the review is updated)
     * Remember to unregister the listener once you don't need data anymore
     */
    override fun getReviewByUserIdAndPlaygroundId(
        userId: String,
        playgroundId: String,
        fireCallback: (FireResult<Review, DefaultGetFireError>) -> Unit
    ): FireListener {

        val listener = db.collection("reviews")
            .whereEqualTo("userId", userId)
            .whereEqualTo("playgroundId", playgroundId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // a Firebase error occurred
                    Log.d("default error", "Error: a generic error occurred retrieving review with userId $userId and playgroundId $playgroundId in FireRepository.getReviewByUserIdAndPlaygroundId(). Message: ${error.message}")
                    fireCallback(DefaultGetFireError.default(
                        "Error: a generic error occurred retrieving the review"
                    ))
                    return@addSnapshotListener
                }

                if(value == null || value.isEmpty) {
                    // review not found
                    Log.d("not found error", "Error: review with userId $userId and playgroundId $playgroundId has not been found in FireRepository.getReviewByUserIdAndPlaygroundId()")
                    fireCallback(DefaultGetFireError.notFound(
                        "Error: review has not been found"
                    ))
                    return@addSnapshotListener
                }

                // * review exists *

                // retrieve the first review document (it should be just one)
                val reviewDocument = value.documents[0]

                // deserialize it and convert it to an entity
                val fireReview = FireReview.deserialize(reviewDocument.id, reviewDocument.data)
                val review = fireReview?.toReview()

                // Note: username is already set in the review, since Review document already contains it

                if (review == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: an error occurred deserializing a review in FireRepository.getReviewByUserIdAndPlaygroundId()")
                    fireCallback(DefaultGetFireError.duringDeserialization(
                        "Error: an error occurred retrieving review data"
                    ))
                    return@addSnapshotListener
                }

                // successfully return review
                fireCallback(Success(review))
            }

        return FireListener(listener)
    }

    /**
     * Insert a new Review document in the Firestore cloud db
     */
    override fun insertOrUpdateReview(
        review: Review,
        fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit
    ) {
        // set lastUpdate and timestamp
        val nowStr = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).toString()
        review.lastUpdate = nowStr

        if(review.id == null)   // new review -> save timestamp
            review.timestamp = nowStr

        // convert entity to fireReview, but username is still empty here
        val fireReview = FireReview.from(review)

        // create firestore docs references
        val userDocRef = db.collection("users").document(review.userId)
        val reviewDocRef =
            if (review.id == null)  // store new review -> new id
                db.collection("reviews").document()
            else    // update existing review -> keep same id
                db.collection("reviews").document(review.id)

        // * No check about user allowed to leave a review here *

        // transaction:
        // 1 - retrieve username
        // 2 - save review, setting that username too
        db.runTransaction { transaction ->
            //  first, retrieve the username from the users collection
            val userDocument = transaction.get(userDocRef)

            if (!userDocument.exists()) {
                throw FirebaseFirestoreException("user document not found", FirebaseFirestoreException.Code.NOT_FOUND)
            }

            val username = userDocument.getString("username")

            @Suppress
            if(username == null) {
                throw FirebaseFirestoreException("deserialization error", FirebaseFirestoreException.Code.INVALID_ARGUMENT)
            }

            // username correctly retrieved here -> save it
            fireReview.username = username

            // now insert or update review
            transaction.set(reviewDocRef, fireReview.serialize())

            Unit
        }.addOnSuccessListener { x ->
            // * review successfully saved *
            fireCallback(Success(x))
        }
        .addOnFailureListener { exception ->
            // create the proper error type and the related message
            val errorType: Error<Unit,DefaultInsertFireError> =
                if (exception is FirebaseFirestoreException &&
                    exception.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    // user not found
                    Log.d("user not found error", "Error: review user with id ${review.userId} has not been found in FireRepository.insertOrUpdateReview() transaction")
                    DefaultInsertFireError.conflict("Error: a conflict error occurred saving the review")
                }
                else if (exception is FirebaseFirestoreException &&
                        exception.code == FirebaseFirestoreException.Code.INVALID_ARGUMENT) {
                    // user deserialization error
                    Log.d("deserialization error", "Error: review user with id ${review.userId} has not been correctly deserialized in FireRepository.insertOrUpdateReview() transaction")
                    DefaultInsertFireError.default("Error: a generic error occurred saving the review")
                }
                else {
                    // firebase generic error
                    Log.d("generic error", "Error: a generic error occurred saving review $review in in FireRepository.insertOrUpdateReview() transaction. Message: ${exception.message}")
                    DefaultInsertFireError.default("Error: a generic error occurred saving the review")
                }

            // propagate error
            fireCallback(errorType)
        }
    }

    /**
     * Delete a review document from the Firestore cloud db
     */
    override fun deleteReview(
        review: Review,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    ) {
        if (review.id == null) {
            // error
            Log.d("generic error", "Error: trying to delete a review with id null, in FireRepository.deleteReview()")
            fireCallback(DefaultFireError.withMessage(
                "Error: an error occurred deleting the review"
            ))
            return
        }

        db.collection("reviews")
            .document(review.id)
            .delete()
            .addOnSuccessListener {
                fireCallback(Success(Unit))
            }
            .addOnFailureListener {
                Log.d("generic error", "Error: a generic error occurred deleting review with id ${review.id} in FireRepository.deleteReview(). Message: ${it.message}")
                fireCallback(DefaultFireError.withMessage("Error: a a generic error occurred deleting the review"))
            }
    }

    /**
     * This method returns a fireResult "true" if the logged user can review the given playground;
     * "false" otherwise.
     * The condition is that the user should have played there almost once in the past, in
     * other words: if exists a reservation involving the user as a participant,
     * whose endTime is before the current time, it returns a fireResult "true".
     * It returns a DefaultFireError in case of an error.
     * */
    override fun loggedUserCanReviewPlayground(
        userId: String,
        playgroundId: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    ) {
        // retrieve user's username first (if it exists)
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener addOnSuccessListener1@ { document ->
                if(document == null || !document.exists()) {
                    // user not found
                    Log.d("not found error", "Error: user with id $userId not found in FireRepository.loggedUserCanReviewPlayground($userId, $playgroundId)")
                    fireCallback(DefaultFireError.withMessage(
                        "Error: user not found"))
                    return@addOnSuccessListener1
                }

                // * user found *
                val user = FireUser.deserialize(document.id, document.data)

                if (user == null) {
                    // deserialization error
                    Log.d("Deserialization error", "Error: an error occurred in user deserialization in FireRepository.loggedUserCanReviewPlayground($userId, $playgroundId)")
                    fireCallback(DefaultFireError.withMessage(
                        "Error: a generic error occurred with user"))
                    return@addOnSuccessListener1
                }

                // retrieved username
                val userToSearchFor = mapOf<String,Any>(
                    "id" to userId,
                    "username" to user.username
                )

                // now filter playground reservations and look for reservations of that playground
                // having the user as a participant
                db.collection("playgroundReservations")
                    .whereEqualTo("playgroundId", playgroundId)
                    .whereArrayContains("participants", userToSearchFor)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents == null) {
                            // generic error
                            Log.d("generic error", "Error: a generic error occurred retrieving playground reservations in FireRepository.loggedUserCanReviewPlayground($userId, $playgroundId)")
                            fireCallback(DefaultFireError.withMessage(
                                "Error: a generic error occurred checking playground reservations"
                            ))
                            return@addOnSuccessListener
                        }

                        // deserialize playground reservations
                        val playgroundReservationsDocuments = documents.map {
                            val rawDocument = FirePlaygroundReservation.deserialize(it.id, it.data)

                            if(rawDocument == null) {
                                // deserialization error
                                Log.d("deserialization error", "Error: an error occurred deserializing a (fire)playgroundReservation in FireRepository.loggedUserCanReviewPlayground($userId, $playgroundId)")
                                fireCallback(DefaultFireError.withMessage(
                                    "Error: a generic error occurred checking playground reservations"))
                                return@addOnSuccessListener
                            }
                            rawDocument
                        }

                        // * user can review the playground only if at least one of
                        // the filtered reservations is already completed (endTime < now()) *

                        val now = LocalDateTime.now()
                        val userCanReviewPlayground = playgroundReservationsDocuments.any { reservation ->
                            try {
                                // filter the ones having endDate before now
                                LocalDateTime.parse(reservation.endDateTime) < now
                            }
                            catch (e: Exception) {
                                Log.d("date time parsing error", "Error parsing playground reservation end time ${reservation.endDateTime} in FireRepository.loggedUserCanReviewPlayground($userId, $playgroundId)")
                                fireCallback(DefaultFireError.withMessage(
                                    "Error: a generic error occurred checking playground reservations"))
                                return@addOnSuccessListener
                            }
                        }

                        // successfully return the result
                        fireCallback(Success(userCanReviewPlayground))
                        return@addOnSuccessListener
                    }
                    .addOnFailureListener {
                        // generic error
                        Log.d("generic error", "Error: a generic error occurred retrieving playground reservations in FireRepository.loggedUserCanReviewPlayground(). Message: ${it.message}")
                        fireCallback(DefaultFireError.withMessage(
                            "Error: a generic error occurred checking playground reservations"
                        ))
                        return@addOnFailureListener
                    }
            }
            .addOnFailureListener {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving user with id $userId in FireRepository.loggedUserCanReviewPlayground(). Message: ${it.message}")
                fireCallback(DefaultFireError.withMessage("Error: a generic error occurred verifying the user"))
                return@addOnFailureListener
            }
    }

    /* reservations */

    /**
     * Retrieve a specific (detailed) reservation from the db, given its id
     * **Note**: the result is **dynamic** (fireCallback is called each time the reservation changes)
     * Remember to unregister the listener once you don't need the reservation anymore
     */
    override fun getDetailedReservationById(
        reservationId: String,
        fireCallback: (FireResult<DetailedReservation, DefaultGetFireError>) -> Unit
    ): FireListener {
        // (1) retrieve the playgroundReservation document **dynamic**
        // (2) retrieve the related playgroundSport document
        // (3) retrieve any equipments documents associated to the reservation **dynamic**
        // (4) combine them to create a DetailedReservation entity

        val equipmentsListener = FireListener()
        val equipmentsListenerLock = Unit

        // 1 - retrieving dynamic playgroundReservation document
        val listener = db.collection("playgroundReservations")
            .document(reservationId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // a generic error occurred
                    Log.d("generic error", "Error: a generic error occurred getting a playgroundReservation snapshot with id $reservationId in FireRepository.getDetailedReservationById(). Message: ${error.message}")
                    fireCallback(DefaultGetFireError.default(
                        "Error: a generic error occurred retrieving playground reservation"
                    ))
                    return@addSnapshotListener
                }

                if (value == null || !value.exists()) {
                    // not found
                    Log.d("not found error", "Error: playgroundReservation with id $reservationId NOT FOUND in FireRepository.getDetailedReservationById()")
                    fireCallback(DefaultGetFireError.notFound(
                        "Error: playground reservation not found"
                    ))
                    return@addSnapshotListener
                }

                // * retrieved playground reservation *

                // deserialize reservation data
                val playgroundReservationDocument = FirePlaygroundReservation.deserialize(value.id, value.data)

                if (playgroundReservationDocument == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: deserialization error occurred for playground reservation with id $reservationId in FireRepository.getDetailedReservationById()")
                    fireCallback(DefaultGetFireError.duringDeserialization(
                        "Error: an error occurred retrieving playground reservation"
                    ))
                    return@addSnapshotListener
                }

                // 2 - retrieving related playgroundSport document
                db.collection("playgroundSports")
                    .document(playgroundReservationDocument.playgroundId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document == null) {
                            // not found
                            Log.d("not found error", "Error: playground sport with id ${playgroundReservationDocument.playgroundId} not found in FireRepository.getDetailedReservationById()")
                            fireCallback(DefaultGetFireError.notFound(
                                "Error: playground sport info not found"
                            ))
                            return@addOnSuccessListener
                        }

                        // * retrieved playground sport *

                        // deserialize playground data
                        val playgroundSportDocument = FirePlaygroundSport.deserialize(document.id, document.data)

                        if (playgroundSportDocument == null) {
                            // deserialization error
                            Log.d("deserialization error", "Error: an error occurred deserializing playground sport with id ${playgroundReservationDocument.playgroundId} in FireRepository.getDetailedReservationById()")
                            fireCallback(DefaultGetFireError.duringDeserialization(
                                "Error: playground not found"
                            ))
                            return@addOnSuccessListener
                        }

                        // 3 - **dynamically** retrieve any equipments documents associated to this reservation

                        synchronized(equipmentsListenerLock) {
                            equipmentsListener.unregister()

                            val internalListener = db.collection("equipmentReservationSlots")
                                .whereEqualTo("playgroundReservationId", reservationId)
                                // take just the first slot documents (the other slots' ones are the same)
                                .whereEqualTo("startSlot", playgroundReservationDocument.startDateTime)
                                .addSnapshotListener addSnapshotListenerInternal@ { value, error ->
                                    if (error != null || value == null) {
                                        // generic error
                                        Log.d("generic error", "Error: a generic error occurred getting reservation equipment slots snapshot for reservation $reservationId in FireRepository.getDetailedReservationById(). Message: ${error?.message}")
                                        fireCallback(DefaultGetFireError.default(
                                            "Error: a generic error occurred retrieving reservation equipments"
                                        ))
                                        return@addSnapshotListenerInternal
                                    }

                                    val equipmentsDocumentsList = mutableListOf<FireEquipmentReservationSlot>()

                                    for (rawEquipmentSlot in value) {
                                        // deserialize each equipment slot document
                                        val equipmentReservationSlotDoc = FireEquipmentReservationSlot.deserialize(rawEquipmentSlot.id, rawEquipmentSlot.data)

                                        if(equipmentReservationSlotDoc == null) {
                                            // deserialization error
                                            Log.d("deserialization error", "Error: an error occurred deserializing equipment reservation slot with id ${rawEquipmentSlot.id} in FireRepository.getDetailedReservationById()")
                                            fireCallback(DefaultGetFireError.duringDeserialization(
                                                "Error: an error occurred retrieving reservation equipments"
                                            ))
                                            return@addSnapshotListenerInternal
                                        }

                                        equipmentsDocumentsList.add(equipmentReservationSlotDoc)
                                    }

                                    // * equipments reservation slot retrieved *

                                    // 4 - combine reservation, playground and equipments documents
                                    // into a DetailedReservation entity
                                    val detailedReservation = playgroundReservationDocument.toDetailedReservation(
                                        playgroundSportDocument,
                                        equipmentsDocumentsList
                                    )

                                    // * return successfully the detailed reservation *
                                    fireCallback(Success(detailedReservation))
                                    return@addSnapshotListenerInternal
                                }

                            equipmentsListener.add(FireListener(internalListener))
                        }
                    }
                    .addOnFailureListener {
                        // generic error
                        Log.d("generic error",  "Error: a generic error occurred retrieving playground sport data for playground reservation $reservationId in FireRepository.getDetailedReservationById(). Message: ${it.message}")
                        fireCallback(DefaultGetFireError.default(
                            "Error: a generic error occurred retrieving playground sport"
                        ))
                        return@addOnFailureListener
                    }
            }

        return FireListener(listener).also {
            it.add(equipmentsListener)
        }
    }


    /**
     * Create a new reservation in the Firestore cloud db, or override the existing one if
     * any (with the same reservationId) Returns a Success with the newReservationId, if
     * the save went well, or an Error if an error occurred
     * - 'newReservationId' is the new id assigned to the reservation (or the
     *   same as the previous one, if any)
     * - 'error' is an instance of NewReservationError reflecting the type of
     *   error occurred
     */
    override fun overrideNewReservation(
        userId: String,
        reservation: NewReservation,
        fireCallback: (FireResult<String, NewReservationError>) -> Unit
    ) {
        // (0.1) retrieve the current user
        this.getStaticUser(userId) { fireResult ->
            if(fireResult.isError()) {
                fireCallback(NewReservationError.unexpected(fireResult.errorMessage()))
                return@getStaticUser
            }

            // * user exists *
            val user = fireResult.unwrap()

            // (0.2) retrieve reservation slots documents references, if any
            this.getReservationSlotsDocumentsReferences(reservation.id) { fireResult2 ->
                if(fireResult2.isError()) {
                    fireCallback(Error(fireResult2.errorType()))
                    return@getReservationSlotsDocumentsReferences
                }

                val reservationSlotsDocumentsRefs = fireResult2.unwrap()

                // (0.3) then retrieve equipment reservation slots documents references, if any
                this.getEquipmentReservationSlotsDocumentsReferences(reservation.id) { fireResult3 ->
                    if(fireResult3.isError()) {
                        fireCallback(Error(fireResult3.errorType()))
                        return@getEquipmentReservationSlotsDocumentsReferences
                    }

                    val equipmentReservationSlotsDocumentsRefs = fireResult3.unwrap()

                    // * Note: no possible query inside transaction *
                    // so first checks happen before the transaction

                    // (1) check slots availabilities (excluding the actual reservation, if any)
                    this.checkSlotsAvailabilities(reservation) { fireResult4 ->
                        if(fireResult4.isError()) {
                            fireCallback(Error(fireResult4.errorType()))
                            return@checkSlotsAvailabilities
                        }

                        // * slots are available here *

                        // (2) check equipments availabilities
                        // (excluding the actual reservation ones', if any)
                        this.checkEquipmentsAvailabilities(reservation) { fireResult5 ->
                            if(fireResult5.isError()) {
                                fireCallback(Error(fireResult5.errorType()))
                                return@checkEquipmentsAvailabilities
                            }

                            // * selected equipments are available *

                            // retrieve all playgrounds of the same sports
                            this.getPlaygroundsBySportId(reservation.sportId) { fireResult6 ->
                                if(fireResult6.isError()) {
                                    fireCallback(NewReservationError.unexpected())
                                    return@getPlaygroundsBySportId
                                }

                                val allSportPlaygrounds = fireResult6.unwrap()

                                // retrieve equipments docs of the selected equipments
                                this.getReservationEquipmentsById(reservation) { fireResult7 ->
                                    if(fireResult7.isError()) {
                                        fireCallback(NewReservationError.unexpected())
                                        return@getReservationEquipmentsById
                                    }

                                    val reservationEquipmentsById = fireResult7.unwrap()

                                    // save reservation data in batch
                                    this.saveNewReservationDataInBatch(
                                        reservationSlotsDocumentsRefs,
                                        equipmentReservationSlotsDocumentsRefs,
                                        reservation,
                                        user,
                                        allSportPlaygrounds,
                                        reservationEquipmentsById
                                    ) { fireResult8 ->
                                        if(fireResult8.isError()) {
                                            fireCallback(NewReservationError.unexpected())
                                            return@saveNewReservationDataInBatch
                                        }

                                        // * everything went well *
                                        val newReservationId = fireResult8.unwrap()

                                        // return the new id
                                        fireCallback(Success(newReservationId))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieve from the Firestore cloud db all the reservations
     * in which the user is involved as a participant
     */
    override fun getReservationsPerDateByUserId(
        userId: String,
        fireCallback: (FireResult<Map<LocalDate, List<DetailedReservation>>, DefaultGetFireError>) -> Unit
    ): FireListener {
        val fireListener = FireListener()

        // first retrieve the user
        this.getUser(userId) { fireResult ->
            if(fireResult.isError()) {
                fireCallback(DefaultGetFireError.default(
                    "Error: an error occurred retrieving the reservations"
                ))
                return@getUser
            }

            val user = fireResult.unwrap()
            val userShortDoc = FireUserForPlaygroundReservation.from(user)

            // * dynamically retrieve all the reservations having the user as one of the participants *
            val listener = this.getPlaygroundReservationsOfUser(userShortDoc) { fireResult2 ->
                if(fireResult2.isError()) {
                    fireCallback(Error(fireResult2.errorType()))
                    return@getPlaygroundReservationsOfUser
                }

                val userPlaygroundReservations = fireResult2.unwrap()
                val playgroundsIds = userPlaygroundReservations.map { it.playgroundId }

                // * now retrieve all the playground sport documents associated to those reservations *
                this.getPlaygroundsByIds(playgroundsIds) { fireResult3 ->
                    if(fireResult3.isError()) {
                        fireCallback(Error(fireResult3.errorType()))
                        return@getPlaygroundsByIds
                    }

                    val reservationPlaygroundSportsById = fireResult3.unwrap().associateBy { it.id }

                    // now combine each reservation with the corresponding playgroundSport,
                    // to create a DetailedReservation entity
                    // **Note**: here we ignore the equipments related to the reservation

                    val userDetailedReservations = userPlaygroundReservations.map { playgroundReservation ->
                        val correspondingPlaygroundSport = reservationPlaygroundSportsById[playgroundReservation.playgroundId]

                        if (correspondingPlaygroundSport == null) {
                            // db consistency error
                            Log.d("consistency error", "Error: reservation playground ${playgroundReservation.playgroundId} does not exist in playgroundSports collection, in FireRepository.getReservationsPerDateByUserId()")
                            fireCallback(DefaultGetFireError.default(
                                "Error: an error occurred retrieving playgrounds info"
                            ))
                            return@getPlaygroundsByIds
                        }

                        val detailedReservation = playgroundReservation.toDetailedReservation(
                            correspondingPlaygroundSport,
                            listOf()    // * ignore reservation equipments here *
                        )

                        detailedReservation
                    }

                    // group reservations by date
                    val userDetailedReservationsByDate = userDetailedReservations.groupBy { reservation ->
                        reservation.date
                    }

                    // * return successfully *
                    fireCallback(Success(userDetailedReservationsByDate))
                }
            }

            fireListener.add(listener)
        }

        return fireListener
    }

    // TODO
    override fun deleteReservation(
        reservation: DetailedReservation,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    // * Equipments methods *

    // TODO
    override fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: String,
        sportId: String,
        reservationId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        fireCallback: (FireResult<MutableMap<String, Equipment>, DefaultFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    // TODO
    override fun getAllEquipmentsBySportCenterIdAndSportId(
        sportCenterId: String,
        sportId: String,
        fireCallback: (FireResult<MutableMap<String, Equipment>, DefaultFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    /* playgrounds */

    override fun getPlaygroundInfoById(
        playgroundId: String,
        fireCallback: (FireResult<PlaygroundInfo, DefaultGetFireError>) -> Unit
    ): FireListener {

        // (1) retrieve the playgroundSports document **dynamic**
        // (2) retrieve the related reviews collection
        // (3) combine them to create a PlaygroundInfo entity

        val reviewListener = FireListener()
        val reviewListenerLock = Unit

        // (1) retrieve the playgroundSports document **dynamic**
        val listener = db.collection("playgroundSports")
            .document(playgroundId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // a generic error occurred
                    Log.d("generic error", "Error: a generic error occurred getting a playgroundSports snapshot with id $playgroundId in FireRepository.getPlaygroundInfoById(). Message: ${error.message}")
                    fireCallback(DefaultGetFireError.default(
                        "Error: a generic error occurred retrieving playground"
                    ))
                    return@addSnapshotListener
                }

                if (value == null || !value.exists()) {
                    // not found
                    Log.d("not found error", "Error: playgroundSports with id $playgroundId NOT FOUND in FireRepository.getPlaygroundSportsById()")
                    fireCallback(DefaultGetFireError.notFound(
                        "Error: playground not found"
                    ))
                    return@addSnapshotListener
                }

                // * retrieved playgroundSports document *

                // deserialize playground data
                val playgroundSportsDocument = FirePlaygroundSport.deserialize(value.id, value.data)

                if(playgroundSportsDocument == null){
                    // deserialization error
                    Log.d("deserialization error", "Error: deserialization error occurred for playground with id $playgroundId in FireRepository.getPlaygroundInfoById()")
                    fireCallback(DefaultGetFireError.duringDeserialization(
                        "Error: an error occurred retrieving playground"
                    ))
                    return@addSnapshotListener
                }

                // (2) retrieve the related reviews collection
                synchronized(reviewListenerLock) {
                    reviewListener.unregister()

                    val internalListener = db.collection("reviews")
                        .whereEqualTo("playgroundId", playgroundId)
                        .addSnapshotListener addSnapshotListenerInternal@ { value, error ->
                            if (error != null || value == null) {
                                // generic error
                                Log.d("generic error", "Error: a generic error occurred getting reviews snapshot for playground $playgroundId in FireRepository.getPlaygroundInfoById(). Message: ${error?.message}")
                                fireCallback(DefaultGetFireError.default(
                                    "Error: a generic error occurred retrieving reviews"
                                ))
                                return@addSnapshotListenerInternal
                            }

                            val reviewsDocumentsList = mutableListOf<FireReview>()

                            if (!value.isEmpty) {
                                for (rawReview in value) {
                                    //deserialize each review document
                                    val reviewDocument =
                                        FireReview.deserialize(rawReview.id, rawReview.data)

                                    if (reviewDocument == null) {
                                        // deserialization error
                                        Log.d(
                                            "deserialization error",
                                            "Error: an error occurred deserializing review with id ${rawReview.id} in FireRepository.getPlaygroundInfoById()"
                                        )
                                        fireCallback(
                                            DefaultGetFireError.duringDeserialization(
                                                "Error: an error occurred retrieving a review"
                                            )
                                        )
                                        return@addSnapshotListenerInternal
                                    }

                                    reviewsDocumentsList.add(reviewDocument)
                                }
                            }

                            // * review list retrieved *

                            // (3) combine them to create a PlaygroundInfo entity
                            val playgroundInfo = playgroundSportsDocument.toPlaygroundInfo(reviewsDocumentsList)

                            if(playgroundInfo == null){
                                // conversion error
                                Log.d("conversion error", "Error: an error occurred converting FirePlaygroundSport with id $playgroundId into PlaygroundInfo in FireRepository.getPlaygroundInfoById()")
                                fireCallback(DefaultGetFireError.duringDeserialization(
                                    "Error: an error occurred retrieving the playground"
                                ))
                                return@addSnapshotListenerInternal
                            }

                            // * return successfully the playground info *
                            fireCallback(Success(playgroundInfo))
                        }

                    reviewListener.add(FireListener(internalListener))
                }
            }

        return FireListener(listener).also {
            it.add(reviewListener)
        }
    }

    // TODO
    override fun getAvailablePlaygroundsPerSlot(
        month: YearMonth,
        sport: Sport?,
        fireCallback: (FireResult<MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>>, DefaultFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    // TODO
    override fun getAllPlaygroundsInfo(
        fireCallback: (FireResult<List<PlaygroundInfo>, DefaultFireError>) -> Unit
    ): FireListener {
        TODO("Not yet implemented")
    }

    /* notifications */

    // TODO
    /**
     * Return a fireListener listening to notifications for the given user
     * The notifications are related to the incoming reservations and NOT the past ones.
     * The fireCallback is called every time a new notification is received.
     */
    override fun getNotificationsByUserId(
        userId: String,
        fireCallback: (FireResult<MutableList<Notification>, DefaultGetFireError>) -> Unit
    ): FireListener {
        val fireListener = FireListener()

        val notificationListener = db.collection("notifications")
            .whereEqualTo("receiverId", userId)
            .addSnapshotListener { documents, error ->
                if (error != null || documents == null) {
                    // firebase error
                    Log.d(
                        "generic error",
                        "Error: a generic error in FireRepository.getNotificationsByUserId($userId). Message: ${error?.message}"
                    )
                    fireCallback(
                        DefaultGetFireError.default(
                            "Error: a generic error occurred retrieving notifications"
                        )
                    )
                    return@addSnapshotListener
                }

                // retrieving all the notifications related to the given user
                val fireNotifications = mutableListOf<FireNotification>()

                documents.forEach { document ->
                    val fireNotification = FireNotification.deserialize(document.id, document.data)

                    if (fireNotification == null) {
                        // deserialization error
                        Log.d(
                            "deserialization error",
                            "Error: deserialization error in FireRepository.getNotificationsByUserId($userId)"
                        )
                        fireCallback(
                            DefaultGetFireError.duringDeserialization(
                                "Error: an error occurred retrieving notifications"
                            )
                        )
                        return@addSnapshotListener
                    }

                    fireNotifications.add(fireNotification)
                }

                // retrieving a list of pairs (notificationId, reservationId)
                val notificationIdReservationId =
                    fireNotifications.map { Pair(it.id!!, it.reservationId) }
                val reservationIds = notificationIdReservationId.map { it.second }

                // retrieving reservations to filter out the past ones
                db.collection("playgroundReservations").whereIn(FieldPath.documentId(),reservationIds).get()
                    .addOnSuccessListener { documents ->
                        if (documents == null) {
                            // generic error
                            Log.d(
                                "generic error",
                                "Error: retrieved null reservations in FireRepository.getNotificationsByUserId($userId)"
                            )
                            fireCallback(
                                DefaultGetFireError.notFound(
                                    "Error: reservation not found"
                                )
                            )
                            return@addOnSuccessListener
                        }

                        val reservations = documents.map {
                            val reservation =
                                FirePlaygroundReservation.deserialize(it.id, it.data)

                            if (reservation == null) {
                                // deserialization error
                                Log.d(
                                    "deserialization error",
                                    "Error: deserialization error in FireRepository.getNotificationsByUserId($userId)"
                                )
                                fireCallback(
                                    DefaultGetFireError.duringDeserialization(
                                        "Error: an error occurred retrieving notifications"
                                    )
                                )
                                return@addOnSuccessListener
                            }

                            reservation
                        }

                        // filtering out the past reservations
                        val incomingReservations = reservations.filter {
                            LocalDateTime.parse(it.startDateTime).isAfter(LocalDateTime.now())
                        }
                        // filtering out the notifications related to the past reservations
                        notificationIdReservationId.forEach { (notificationId, reservationId) ->
                            // if the reservation is not in the incoming reservations list, the notification is removed
                            if (incomingReservations.none { it.id == reservationId }) {
                                fireNotifications.removeIf { it.id == notificationId }
                            }
                        }

                    }
                    .addOnFailureListener {
                        // generic error
                        Log.d(
                            "generic error",
                            "Error: generic error in FireRepository.getNotificationsByUserId($userId). Message: ${it.message}"
                        )
                        fireCallback(
                            DefaultGetFireError.default(
                                "Error: an error occurred retrieving reservations"
                            )
                        )
                    }

                // converting fireNotifications to notifications
                val notifications = fireNotifications.map {
                    val notification = it.toNotification()
                    if (notification == null) {
                        fireCallback(
                            DefaultGetFireError.duringDeserialization(
                                "Error: an error occurred retrieving notifications"
                            )
                        )
                        return@addSnapshotListener
                    }
                    notification
                }.toMutableList()

                fireCallback(Success(notifications))
            }

        fireListener.add(notificationListener)
        return fireListener
    }

    // TODO
    /**
     * Update invitation status and corresponding reservation participants, based on the old and
     * the new invitation status:
     * - if newStatus is ACCEPTED -> update notification status and **insert** new user as a
     *  reservation's participant (in this case, oldStatus is always PENDING,
     *  since user cannot accept invitation after a refuse)
     * - if newStatus is REJECTED and oldStatus is PENDING -> just update notification status
     *  (user is answering for the first time)
     * - if newStatus is REJECTED and oldStatus is ACCEPTED -> update notification status and **remove**
     *  user from reservation's participants
     */
    override fun updateInvitationStatus(
        notificationId: String,
        oldStatus: NotificationStatus,
        newStatus: NotificationStatus,
        reservationId: String,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    ) {
        db.collection("notifications")
            .document(notificationId)
            .update("status", newStatus.ordinal.toLong())
            .addOnSuccessListener {
                fireCallback(Success(Unit))
            }
            .addOnFailureListener {
                // generic error
                Log.d(
                    "generic error",
                    "Error: generic error in FireRepository.updateNotificationStatus($notificationId, $newStatus). Message: ${it.message}"
                )
                fireCallback(
                    DefaultFireError.withMessage(
                        "Error: a generic error occurred updating notification status"
                    )
                )
            }
    }

    /**
     * (1) Save a new invitation to the db
     * (2) send the corresponding push notification to the receiver
     */
    override fun saveAndSendInvitation(
        notification: Notification,
        fireCallback: (FireResult<Unit, DefaultInsertFireError>) -> Unit
    ) {
        // retrieve current user
        this.getUser(notification.senderUid) { fireResult ->
            if(fireResult.isError()) {
                fireCallback(DefaultInsertFireError.default(
                    "Error: an error occurred retrieving user info"
                ))
                return@getUser
            }

            val user = fireResult.unwrap()

            // set notification's profile image URL
            notification.profileUrl = user.imageURL

            this.saveInvitation(notification) { fireResult2 ->
                if(fireResult2.isError()) {
                    fireCallback(DefaultInsertFireError.default(
                        "Error: an error occurred saving invitation"
                    ))
                    return@saveInvitation
                }

                // * notification saved successfully here *

                // retrieve receiver user
                this.getUser(notification.receiverUid) getUser2@ { fireResult3 ->
                    if(fireResult3.isError()) {
                        fireCallback(DefaultInsertFireError.default(
                            "Error: an error occurred sending push notification"
                        ))
                        return@getUser2
                    }

                    val receiverUser = fireResult3.unwrap()

                    if (receiverUser.notificationsToken == null) {
                        // no token -> user did not give permissions to send push notifications
                        // return without sending push notification
                        fireCallback(Success(Unit))
                        return@getUser2
                    }

                    // * create and send push notification to the receiver *

                    this.createInvitationNotification(
                        receiverUser.notificationsToken,
                        notification.reservationId,
                        notification.description,
                        notification.timestamp
                    ) { fireResult4 ->
                        if(fireResult4.isError()) {
                            // generic error
                            fireCallback(Error(fireResult4.errorType()))
                            return@createInvitationNotification
                        }

                        // * push notification successfully sent *
                        fireCallback(Success(Unit))
                    }
                }
            }
        }
    }
}


