package it.polito.mad.sportapp.model

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import it.polito.mad.sportapp.entities.Achievement
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.FireEquipment
import it.polito.mad.sportapp.entities.firestore.FireEquipmentReservationSlot
import it.polito.mad.sportapp.entities.firestore.FireNotification
import it.polito.mad.sportapp.entities.firestore.FirePlaygroundReservation
import it.polito.mad.sportapp.entities.firestore.FirePlaygroundSport
import it.polito.mad.sportapp.entities.firestore.FireReservationSlot
import it.polito.mad.sportapp.entities.firestore.FireReview
import it.polito.mad.sportapp.entities.firestore.FireUser
import it.polito.mad.sportapp.entities.firestore.FireUserForPlaygroundReservation
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultInsertFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult.*
import it.polito.mad.sportapp.entities.firestore.utilities.NewReservationError
import it.polito.mad.sportapp.entities.firestore.utilities.SaveAndSendInvitationFireError
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


/* user */

internal fun FireRepository.getStaticUser(
    userId: String,
    fireCallback: (FireResult<User, DefaultGetFireError>) -> Unit
) {
    db.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document == null || !document.exists()) {
                // no data exists
                fireCallback(
                    DefaultGetFireError.notFound(
                        "Error: User has not been found"
                    ))
                return@addOnSuccessListener
            }

            // * user exists *

            // deserialize data from db
            val fireUser = FireUser.deserialize(document.id, document.data)

            if (fireUser == null) {
                // deserialization error
                Log.d("deserialization error", "Error: a generic error occurred deserializing user with id $userId in FireRepository.getUser()")

                fireCallback(
                    DefaultGetFireError.duringDeserialization(
                    "Error: a generic error occurred retrieving user"
                ))
                return@addOnSuccessListener
            }

            // * user correctly retrieved *

            // transform to user entity
            val user = fireUser.toUser()

            // compute user achievements (statically)
            this.buildAchievements(userId, user.username) { result ->
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
        .addOnFailureListener {
            Log.d("default error", "Error: a generic error occurred retrieving user with id $userId in FireRepository.getStaticUser(). Message: ${it.message}")
            fireCallback(
                DefaultGetFireError.default(
                "Error: a generic error occurred retrieving user",
            ))
            return@addOnFailureListener
        }
}

// TODO
internal fun FireRepository.buildAchievements(
    userId: String,
    username: String,
    fireCallback: (FireResult<Map<Achievement, Boolean>, DefaultGetFireError>) -> Unit
) {
    db.collection("playgroundReservations").whereEqualTo("user.id", userId).get().addOnSuccessListener { res ->
        if (res == null) {
            // generic error
            Log.d(
                "generic error",
                "Error: a generic error occurred retrieving playground reservations in FireRepository.buildAchievements()"
            )
            fireCallback(DefaultGetFireError.default("Error: a generic error occurred retrieving playground reservations"))
            return@addOnSuccessListener
        }


        val ownedReservations = res.documents.map { FirePlaygroundReservation.deserialize(it.id, it.data) }

        //Now I consider only the past reservations
        var playedGames = ownedReservations.filter {
            val startDateTime = LocalDateTime.parse(it?.startDateTime, DateTimeFormatter.ISO_DATE_TIME)
            startDateTime.isBefore(LocalDateTime.now())
        }.size

        val playgroundIds = ownedReservations.map { it?.playgroundId }.distinct().toMutableList()

        db.collection("playgroundReservation").whereArrayContains("participants", mapOf("id" to userId, "username" to username)).get().addOnSuccessListener {res ->
            if (res == null) {
                // generic error
                Log.d(
                    "generic error",
                    "Error: a generic error occurred retrieving playground reservations in FireRepository.buildAchievements()"
                )
                fireCallback(DefaultGetFireError.default("Error: a generic error occurred retrieving playground reservations"))
                return@addOnSuccessListener
            }
            val participatedReservation = res.documents.map { FirePlaygroundReservation.deserialize(it.id, it.data) }

            //Now I consider only the past reservations
            var participatedGames = participatedReservation.filter {
                val startDateTime = LocalDateTime.parse(it?.startDateTime, DateTimeFormatter.ISO_DATE_TIME)
                startDateTime.isBefore(LocalDateTime.now())
            }.size

            // Now I can calculate the total number of games played
            playedGames += participatedGames

            playgroundIds.addAll(participatedReservation.map { it?.playgroundId }.distinct().toMutableList())

            // Now I can calculate the total number of sports played through the playgrounds

            db.collection("playgroundSports").whereIn(FieldPath.documentId(),playgroundIds).get().addOnSuccessListener { res ->
                if (res == null) {
                    // generic error
                    Log.d(
                        "generic error",
                        "Error: a generic error occurred retrieving playground reservations in FireRepository.buildAchievements()"
                    )
                    fireCallback(DefaultGetFireError.default("Error: a generic error occurred retrieving playground reservations"))
                    return@addOnSuccessListener
                }

                // Now I map the playgrounds
                val firePlaygroundSports = res.documents.map { FirePlaygroundSport.deserialize(it.id, it.data) }

                // Now I can calculate the total number of sports played through the playgrounds
                val playedSports = firePlaygroundSports.map { it?.sport }.distinct().size

                // Now I can calculate the achievements retrieving the total number of sports
                db.collection("sports").get().addOnSuccessListener { res ->
                if (res == null) {
                    // generic error
                    Log.d(
                        "generic error",
                        "Error: a generic error occurred retrieving playground reservations in FireRepository.buildAchievements()"
                    )
                    fireCallback(DefaultGetFireError.default("Error: a generic error occurred retrieving playground reservations"))
                    return@addOnSuccessListener
                }
                    val sportCount = res.documents.size
                    val achievements = mapOf<Achievement, Boolean>(
                        Achievement.AtLeastOneSport to (playedSports >= 1),
                        Achievement.AtLeastFiveSports to (playedSports >= 5),
                        Achievement.AllSports to (playedSports >= sportCount),
                        Achievement.AtLeastThreeMatches to (playedGames >= 2),
                        Achievement.AtLeastTenMatches to (playedGames >= 5),
                        Achievement.AtLeastTwentyFiveMatches to (playedGames >= 25),
                    )

                    // Now I can return the achievements
                    fireCallback(Success(achievements))


                }.addOnFailureListener{
                    // generic error
                    Log.d(
                        "generic error",
                        "Error: a generic error occurred retrieving playground reservations in FireRepository.buildAchievements(). Message: ${it.message}"
                    )
                    fireCallback(DefaultGetFireError.default("Error: a generic error occurred retrieving sports"))
                    return@addOnFailureListener
                }




            }.addOnFailureListener {
                // generic error
                Log.d(
                    "generic error",
                    "Error: a generic error occurred retrieving playground reservations in FireRepository.buildAchievements(). Message: ${it.message}"
                )
                fireCallback(DefaultGetFireError.default("Error: a generic error occurred retrieving playground reservations"))
                return@addOnFailureListener

            }
        }
            .addOnFailureListener {
                Log.d(
                    "generic error",
                    "Error: a generic error occurred retrieving playground reservations in FireRepository.buildAchievements(). Message: ${it.message}"
                )
                fireCallback(DefaultGetFireError.default("Error: a generic error occurred retrieving playground reservations"))
                return@addOnFailureListener
            }

    }.addOnFailureListener {
        // generic error
        Log.d(
            "generic error",
            "Error: a generic error occurred retrieving playground reservations in FireRepository.buildAchievements(). Message: ${it.message}"
        )
        fireCallback(DefaultGetFireError.default("Error: a generic error occurred retrieving playground reservations"))
    }






}

/* reviews */
internal fun FireRepository.getAllReviewsByPlaygroundId(
    fireCallback: (FireResult< Map<String,List<FireReview>>, DefaultGetFireError >) -> Unit
) {
    db.collection("reviews")
        .get()
        .addOnSuccessListener { res ->
            if(res == null) {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving all the reviews in FireRepository.getAllReviewsByPlaygroundId()")
                fireCallback(DefaultGetFireError.default(
                    "Error: a generic error occurred retrieving all the reviews"
                ))
                return@addOnSuccessListener
            }

            val allReviews = res.map { doc ->
                val deserializedDoc = FireReview.deserialize(doc.id, doc.data)

                if (deserializedDoc == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: an error occurred deserializing review doc ${doc.id} in FireRepository.getAllReviewsByPlaygroundId()")
                    fireCallback(DefaultGetFireError.duringDeserialization(
                        "Error: an error occurred retrieving all the reviews"
                    ))
                    return@addOnSuccessListener
                }

                deserializedDoc
            }

            val allReviewsByPlaygroundId = allReviews.groupBy { it.playgroundId }

            // return successfully
            fireCallback(Success(allReviewsByPlaygroundId))
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred retrieving all the reviews in FireRepository.getAllReviews(). Message: ${it.message}")
            fireCallback(DefaultGetFireError.default(
                "Error: a generic error occurred retrieving all the reviews"
            ))
            return@addOnFailureListener
        }
}

/* reservations */

internal fun FireRepository.getReservationSlotsDocumentsReferences(
    reservationId: String?,
    fireCallback: (FireResult<List<DocumentReference>, NewReservationError>) -> Unit
) {
    if (reservationId == null) {
        fireCallback(Success(mutableListOf()))
        return
    }

    db.collection("reservationSlots")
        .whereEqualTo("reservationId", reservationId)
        .get()
        .addOnSuccessListener { res ->
            if (res == null) {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving reservation slots references in FireRepository.getReservationSlotsDocumentsReferences()")
                fireCallback(NewReservationError.unexpected())
                return@addOnSuccessListener
            }

            val reservationSlotsDocumentsRefs = res.map { it.reference }
            fireCallback(Success(reservationSlotsDocumentsRefs))
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred retrieving reservation slots references in FireRepository.getReservationSlotsDocumentsReferences(). Message: ${it.message}")
            fireCallback(NewReservationError.unexpected())
        }
}

internal fun FireRepository.getEquipmentReservationSlotsDocumentsReferences(
    reservationId: String?,
    fireCallback: (FireResult<List<DocumentReference>, NewReservationError>) -> Unit
) {
    if (reservationId == null) {
        fireCallback(Success(mutableListOf()))
        return
    }

    db.collection("equipmentReservationSlots")
        .whereEqualTo("playgroundReservationId", reservationId)
        .get()
        .addOnSuccessListener { res ->
            if (res == null) {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving equipment reservation slots references in FireRepository.getEquipmentReservationSlotsDocumentsReferences()")
                fireCallback(NewReservationError.unexpected())
                return@addOnSuccessListener
            }

            val equipmentReservationSlotsDocumentsRefs = res.map { it.reference }
            fireCallback(Success(equipmentReservationSlotsDocumentsRefs))
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred retrieving equipment reservation slots references in FireRepository.getEquipmentReservationSlotsDocumentsReferences(). Message: ${it.message}")
            fireCallback(NewReservationError.unexpected())
        }
}

internal fun FireRepository.checkSlotsAvailabilities(
    reservation: NewReservation,
    fireCallback: (FireResult<Unit, NewReservationError>) -> Unit
) {
    // search for any busy slot, of that playground, contained in
    // [reservation.StartTime, reservation.EndTime],
    // excluding the ones of the reservation itself (if any)
    db.collection("reservationSlots")
        .whereEqualTo("playgroundId", reservation.playgroundId)
        .whereGreaterThanOrEqualTo("startSlot", reservation.startTime.format(DateTimeFormatter.ISO_DATE_TIME))
        .whereLessThanOrEqualTo("endSlot", reservation.endTime.format(DateTimeFormatter.ISO_DATE_TIME))
        .get()
        .addOnSuccessListener { res ->
            if (res == null) {
                // generic error
                Log.d(
                    "generic error",
                    "Error: a generic error occurred retrieving reservationSlots in FireRepository.checkSlotsAvailabilities()"
                )
                fireCallback(NewReservationError.unexpected())
                return@addOnSuccessListener
            }

            // deserialize reservation slots
            var reservationSlotsDocuments = res.map { rawDocument ->
                val deserializedDocument =
                    FireReservationSlot.deserialize(rawDocument.id, rawDocument.data)

                if (deserializedDocument == null) {
                    // deserialization error
                    Log.d(
                        "deserialization error",
                        "Error: an error occurred deserializing reservation slot $rawDocument in FireRepository.checkSlotsAvailabilities()"
                    )
                    fireCallback(NewReservationError.unexpected())
                    return@addOnSuccessListener
                }

                deserializedDocument
            }

            // filter slots occupied by the current existing reservation, if any
            reservationSlotsDocuments = reservationSlotsDocuments.filter { doc ->
                doc.reservationId != reservation.id
            }

            // if any slots exists, they are *busy*, so there is a conflict in the reservation booking
            if (reservationSlotsDocuments.isNotEmpty()) {
                // slots conflict: the slots you are trying to reserve are already busy!
                Log.d(
                    "conflict error",
                    "A conflict emerged checking available slots, in FireRepository.overrideNewReservation()"
                )
                fireCallback(NewReservationError.slotConflict())
                return@addOnSuccessListener
            }

            // * slots are available *
            fireCallback(Success(Unit))
            return@addOnSuccessListener
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred retrieving reservationSlots in FireRepository.checkSlotsAvailabilities(). Message: ${it.message}")
            fireCallback(NewReservationError.unexpected())
            return@addOnFailureListener
        }
}

internal fun FireRepository.checkEquipmentsAvailabilities(
    reservation: NewReservation,
    fireCallback: (FireResult<Unit, NewReservationError>) -> Unit
) {
    // check for equipments availability: retrieve all the equipments slots
    // in [reservation.startTime, reservation.endTime] related to the requested equipments,
    // excluding the slots of this reservation (if any); then, for each slot and equipments
    // compute the remaining qty, and check if it is < of the requested one; in this case,
    // the equipments are NOT available for the new reservation
    db.collection("equipmentReservationSlots")
        .whereGreaterThanOrEqualTo("startSlot", reservation.startTime.format(DateTimeFormatter.ISO_DATE_TIME))
        .whereLessThanOrEqualTo("endSlot", reservation.endTime.format(DateTimeFormatter.ISO_DATE_TIME))
        .whereIn("equipment.id", reservation.selectedEquipments.map { it.equipmentId })
        .get()
        .addOnSuccessListener { result ->
            if (result == null) {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving equipment reservation slots in FireRepository.checkEquipmentsAvailabilities()")
                fireCallback(NewReservationError.unexpected())
                return@addOnSuccessListener
            }

            // deserialize equipment reservation slots documents
            var equipmentReservationSlotsDocs = result.map { doc ->
                val equipmentReservationSlotDoc = FireEquipmentReservationSlot.deserialize(doc.id, doc.data)

                if(equipmentReservationSlotDoc == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: an error occurred deserializing equipment reservation slot $doc in FireRepository.checkEquipmentsAvailabilities()")
                    fireCallback(NewReservationError.unexpected())
                    return@addOnSuccessListener
                }

                equipmentReservationSlotDoc
            }

            // filter the slots of the current reservation, if any
            equipmentReservationSlotsDocs = equipmentReservationSlotsDocs.filter { doc ->
                doc.playgroundReservationId != reservation.id
            }

            val selectedEquipmentsQuantities = reservation.selectedEquipments.associate {
                Pair(it.equipmentId, it.selectedQuantity)
            }

            // check if in each slots, selected quantities are available
            equipmentReservationSlotsDocs.groupBy { equipmentReservationSlot ->
                equipmentReservationSlot.startSlot
            }.forEach { (_, equipmentReservations) ->
                equipmentReservations.groupBy(
                    // group by equipment id
                    { it.equipment.id },
                    { Pair(it.selectedQuantity, it.equipment.maxQuantity) }
                ).forEach { (equipmentId, quantities) ->
                    // for each equipment (in each slot) compute the available qty
                    // check if the selected qty exceeds the available one; if so, return equipment conflict error
                    val totalOccupiedQty = quantities.sumOf { (selectedQty, _) -> selectedQty }
                    val maxQty = quantities[0].second

                    // compute available quantity
                    val availableQty = maxQty - totalOccupiedQty

                    val selectedQty = selectedEquipmentsQuantities[equipmentId]!!   // equipment must be there

                    if(selectedQty > availableQty) {
                        // selected qty for this reservation *exceeds* the available one -> equipment conflict
                        Log.d("equipment conflict", "Error: selected qty $selectedQty exceeds available qty $availableQty for equipment $equipmentId, in FireRepository.checkEquipmentsAvailabilities()")
                        fireCallback(NewReservationError.equipmentConflict())
                        return@addOnSuccessListener
                    }
                }
            }

            // * selected equipments are available *

            fireCallback(Success(Unit))
            return@addOnSuccessListener
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred retrieving equipment reservation slots in FireRepository.checkEquipmentsAvailabilities(). Message: ${it.message}")
            fireCallback(NewReservationError.unexpected())
            return@addOnFailureListener
        }
}

internal fun FireRepository.saveNewReservationDataInBatch(
    reservationSlotsDocumentsRefs: List<DocumentReference>,
    equipmentReservationSlotsDocumentsRefs: List<DocumentReference>,
    reservation: NewReservation,
    user: User,
    sportPlaygrounds: List<FirePlaygroundSport>,
    reservationEquipmentsById: Map<String, FireEquipment>,
    fireCallback: (FireResult<String, DefaultInsertFireError>) -> Unit
) {
    db.runTransaction { transaction ->
        // (3) delete old reservation slots (if any)
        reservationSlotsDocumentsRefs.forEach(transaction::delete)

        // (4) delete old equipment reservation slots (if any)
        equipmentReservationSlotsDocumentsRefs.forEach(transaction::delete)

        // (5) create or update existing playground reservation
        val firePlaygroundReservation = FirePlaygroundReservation.fromNewReservation(
            reservation,
            FireUserForPlaygroundReservation.from(user)
        )

        val newReservationId = if (reservation.id != null) {
            // update existing playground reservation
            val docRef = db.collection("playgroundReservations").document(reservation.id)

            // update everything except for user and participants
            val dataToUpdate = firePlaygroundReservation.serialize()
                .toMutableMap()

            dataToUpdate.remove("user")
            dataToUpdate.remove("participants")

            transaction.update(docRef, dataToUpdate)

            reservation.id
        }
        else {
            // set new playground reservation
            val newDocRef = db.collection("playgroundReservations").document()
            transaction.set(newDocRef, firePlaygroundReservation.serialize())

            newDocRef.id
        }

        // (6) save new reservation slots
        val newSlots = FireReservationSlot.slotsFromNewReservation(
            reservation,
            newReservationId
        )

        // fill these slots' open playgrounds ids
        newSlots.forEach { fireReservationSlot ->
            fireReservationSlot.openPlaygroundsIds = sportPlaygrounds.filter { playground ->
                val openingTime = LocalTime.parse(playground.sportCenter.openingHours)
                val closingTime = LocalTime.parse(playground.sportCenter.closingHours)
                val startSlot = LocalDateTime.parse(fireReservationSlot.startSlot).toLocalTime()
                val endSlot = LocalDateTime.parse(fireReservationSlot.endSlot).toLocalTime()

                startSlot >= openingTime && endSlot <= closingTime
            }
                .map { playground -> playground.id }
        }

        newSlots.forEach { newSlot ->
            val newDocRef = db.collection("reservationSlots").document()
            transaction.set(newDocRef, newSlot.serialize())
        }

        // (7) save new equipment reservation slots
        val newEquipmentSlots = FireEquipmentReservationSlot.slotsFromNewReservation(
            reservation,
            newReservationId,
            reservationEquipmentsById
        )

        newEquipmentSlots.forEach { newSlot ->
            val newDocRef = db.collection("equipmentReservationSlots").document()
            transaction.set(newDocRef, newSlot.serialize())
        }

        // every save has been scheduled -> return new assigned reservation id

        newReservationId
    }.addOnSuccessListener { newReservationId ->
        // * everything was successfully saved *
        fireCallback(Success(newReservationId))
    }.addOnFailureListener {
        // generic error
        Log.d("generic error", "Error: a generic error occurred saving new reservation data in FireRepository.saveNewReservationDataInBatch(). Message: ${it.message}")
        fireCallback(
            DefaultInsertFireError.default(
            "Error: an unexpected error occurred saving the reservation"
        ))
        return@addOnFailureListener
    }
}

internal fun FireRepository.getReservationEquipmentsById(
    reservation: NewReservation,
    fireCallback: (FireResult<Map<String, FireEquipment>, DefaultGetFireError>) -> Unit
) {
    db.collection("equipments")
        .whereEqualTo("sportId", reservation.sportId)
        .whereEqualTo("sportCenterId", reservation.sportCenterId)
        .get()
        .addOnSuccessListener { res ->
            if(res == null) {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving equipments in FireRepository.getReservationEquipmentsById()")
                fireCallback(DefaultGetFireError.default(
                    "Error: a generic error occurred retrieving equipments"
                ))
                return@addOnSuccessListener
            }

            val equipmentsDocuments = res.map { doc ->
                val deserializedDoc = FireEquipment.deserialize(doc.id, doc.data)

                if(deserializedDoc == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: an error occurred deserializing equipment $doc in FireRepository.getReservationEquipmentsById()")
                    fireCallback(DefaultGetFireError.duringDeserialization(
                        "Error: an error occurred retrieving equipments"
                    ))
                    return@addOnSuccessListener
                }

                deserializedDoc
            }

            val ids = reservation.selectedEquipments.map { it.equipmentId }

            val selectedEquipmentsDocByIds = equipmentsDocuments.filter {
                ids.contains(it.id)
            }.associateBy { it.id }


            fireCallback(Success(selectedEquipmentsDocByIds))
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred retrieving equipments in FireRepository.getReservationEquipmentsById(). Message: ${it.message}")
            fireCallback(DefaultGetFireError.default(
                "Error: a generic error occurred retrieving equipments"
            ))
            return@addOnFailureListener
        }
}

internal fun FireRepository.getPlaygroundReservationsOfUser(
    user: FireUserForPlaygroundReservation,
    fireCallback: (FireResult<List<FirePlaygroundReservation>, DefaultGetFireError>) -> Unit
): FireListener {
    val listener = db.collection("playgroundReservations")
        .whereArrayContains("participants", user.serialize())
        .addSnapshotListener { value, error ->
            if (error != null || value == null) {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving all the user playground reservations, for user $user, in FireRepository.getPlaygroundReservationsOfUser()")
                fireCallback(DefaultGetFireError.default(
                    "Error: a generic error occurred retrieving user reservations"
                ))
                return@addSnapshotListener
            }

            val userReservations = value.map { doc ->
                val deserializedDoc = FirePlaygroundReservation.deserialize(doc.id, doc.data)

                if (deserializedDoc == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: an error occurred deserializing playground reservation $doc in FireRepository.getPlaygroundReservationsOfUser(${user.id})")
                    fireCallback(DefaultGetFireError.default(
                        "Error: an error occurred retrieving user reservations"
                    ))
                    return@addSnapshotListener
                }

                deserializedDoc
            }

            fireCallback(Success(userReservations))
        }

    return FireListener(listener)
}

internal fun FireRepository.getPlaygroundReservationsByIds(
    reservationsIds: List<String>,
    fireCallback: (FireResult<List<FirePlaygroundReservation>, DefaultGetFireError>) -> Unit
) {
    db.collection("playgroundReservations")
        .whereIn(FieldPath.documentId(), reservationsIds)
        .get()
        .addOnSuccessListener { documents ->
            if (documents == null) {
                // generic error
                Log.d(
                    "generic error",
                    "Error: retrieved null reservations in FireRepository.getPlaygroundReservationsByIds()"
                )
                fireCallback(
                    DefaultGetFireError.notFound(
                        "Error: reservations not found"
                    )
                )
                return@addOnSuccessListener
            }

            val reservations = documents.map {
                val reservation = FirePlaygroundReservation.deserialize(it.id, it.data)

                if (reservation == null) {
                    // deserialization error
                    Log.d(
                        "deserialization error",
                        "Error: deserialization error for reservation $it in FireRepository.getPlaygroundReservationsByIds()"
                    )
                    fireCallback(
                        DefaultGetFireError.duringDeserialization(
                            "Error: an error occurred retrieving user notifications"
                        )
                    )
                    return@addOnSuccessListener
                }

                reservation
            }

            // * return playground reservations successfully *
            fireCallback(Success(reservations))
        }
        .addOnFailureListener {
            // generic error
            Log.d(
                "generic error",
                "Error: a generic error occurred in FireRepository.getPlaygroundReservationsByIds(). Message: ${it.message}"
            )
            fireCallback(
                DefaultGetFireError.default(
                    "Error: an error occurred retrieving user reservations"
                )
            )
        }
}

/* equipments */

internal fun FireRepository.getStaticAllEquipmentsBySportCenterIdAndSportId(
    sportCenterId: String,
    sportId: String,
    fireCallback: (FireResult<List<FireEquipment>, DefaultGetFireError>) -> Unit
) {
    db.collection("equipments")
        .whereEqualTo("sportCenterId", sportCenterId)
        .whereEqualTo("sportId", sportId)
        .get()
        .addOnSuccessListener { res ->
            if(res == null) {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving all the equipments in FireRepository.getStaticAllEquipmentsBySportCenterIdAndSportId($sportCenterId, $sportId)")
                fireCallback(DefaultGetFireError.default(
                    "Error: a generic error occurred retrieving the equipments"
                ))
                return@addOnSuccessListener
            }

            val allEquipments = res.map { doc ->
                val deserializedDoc = FireEquipment.deserialize(doc.id, doc.data)

                if(deserializedDoc == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: an error occurred deserializing an equipment (${doc.id}) in FireRepository.getStaticAllEquipmentsBySportCenterIdAndSportId($sportCenterId, $sportId)")
                    fireCallback(DefaultGetFireError.duringDeserialization(
                        "Error: an error occurred retrieving the equipments"
                    ))
                    return@addOnSuccessListener
                }

                deserializedDoc
            }

            // * return all the equipments documents successfully *
            fireCallback(Success(allEquipments))
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred retrieving all the equipments in FireRepository.getStaticAllEquipmentsBySportCenterIdAndSportId($sportCenterId, $sportId). Message: ${it.message}")
            fireCallback(DefaultGetFireError.default(
                "Error: a generic error occurred retrieving the equipments"
            ))
            return@addOnFailureListener
        }
}

internal fun FireRepository.getDynamicEquipmentReservationSlots(
    sportCenterId: String,
    sportId: String,
    reservationId: String,
    date: String,
    startDateTime: String,
    endDateTime: String,
    fireCallback: (FireResult<List<FireEquipmentReservationSlot>, DefaultGetFireError>) -> Unit
): FireListener {
    val listener = db.collection("equipmentReservationSlots")
        .whereEqualTo("sportCenterId", sportCenterId)
        .whereEqualTo("sportId", sportId)
        .whereEqualTo("date", date)     // filter just the slots of the interesting date
        .addSnapshotListener { value, error ->
            if(error != null || value == null) {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving all the equipment reservation slots in FireRepository.getDynamicEquipmentReservationSlots(). Message: ${error?.message}")
                fireCallback(DefaultGetFireError.default(
                    "Error: a generic error occurred retrieving the equipment reservations"
                ))
                return@addSnapshotListener
            }

            // take just the slots happening in the reservation time [startDateTime,endDateTime]
            // and without considering the slots of the given reservation
            val interestingEquipmentReservationSlots = mutableListOf<FireEquipmentReservationSlot>()

            for (rawDoc in value) {
                val deserializedSlotDoc = FireEquipmentReservationSlot.deserialize(rawDoc.id, rawDoc.data)

                if(deserializedSlotDoc == null){
                    // deserialization error
                    Log.d("deserialization error", "Error: an error occurred deserializing equipment reservation slot $rawDoc in FireRepository.getDynamicEquipmentReservationSlots()")
                    fireCallback(DefaultGetFireError.duringDeserialization(
                        "Error: an error occurred retrieving the equipments availabilities"
                    ))
                    return@addSnapshotListener
                }

                // filter by time and by reservationId
                if(deserializedSlotDoc.playgroundReservationId == reservationId ||
                    deserializedSlotDoc.endSlot <= startDateTime ||
                    deserializedSlotDoc.startSlot >= endDateTime) {
                    continue
                }

                interestingEquipmentReservationSlots.add(deserializedSlotDoc)
            }

            // * return successfully all the interesting equipments reservation slots *
            fireCallback(Success(interestingEquipmentReservationSlots))
        }

    return FireListener(listener)
}

/* playgrounds */

internal fun FireRepository.getPlaygroundsBySportId(
    sportId: String,
    fireCallback: (FireResult<List<FirePlaygroundSport>, DefaultGetFireError>) -> Unit
) {
    db.collection("playgroundSports")
        .whereEqualTo("sport.id", sportId)
        .get()
        .addOnSuccessListener { res ->
            if (res == null) {
                // generic error
                Log.d("generic error", "Error: generic error occurred retrieving playground sports with sport id $sportId in FireRepository.getPlaygroundsBySportId()")
                fireCallback(DefaultGetFireError.default(
                    "Error: a generic error occurred retrieving playgrounds"
                ))
                return@addOnSuccessListener
            }

            val playgroundsDocs = res.map {
                val deserializedDoc = FirePlaygroundSport.deserialize(it.id, it.data)

                if(deserializedDoc == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: deserialization error occurred deserializing playground sport $it in FireRepository.getPlaygroundsBySportId()")
                    fireCallback(DefaultGetFireError.default(
                        "Error: a generic error occurred retrieving playgrounds"
                    ))
                    return@addOnSuccessListener
                }

                deserializedDoc
            }

            fireCallback(Success(playgroundsDocs))
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: generic error occurred retrieving playground sports with sport id $sportId in FireRepository.getPlaygroundsBySportId(). Message: ${it.message}")
            fireCallback(DefaultGetFireError.default(
                "Error: a generic error occurred retrieving playgrounds"
            ))
        }
}

internal fun FireRepository.getPlaygroundsByIds(
    ids: List<String>,
    fireCallback: (FireResult<List<FirePlaygroundSport>, DefaultGetFireError>) -> Unit
) {
    db.collection("playgroundSports")
        .whereIn(FieldPath.documentId(), ids)
        .get()
        .addOnSuccessListener { res ->
            if(res == null) {
                // generic error
                Log.d("generic error", "Error: a generic error occurred retrieving all the reservations playgrounds with ids:$ids, in FireRepository.getPlaygroundsBySportId()")
                fireCallback(DefaultGetFireError.default(
                    "Error: an error occurred retrieving playgrounds info"
                ))
                return@addOnSuccessListener
            }

            val playgroundSportsDocs = res.map { doc ->
                val deserializedDoc = FirePlaygroundSport.deserialize(doc.id, doc.data)

                if (deserializedDoc == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: an error occurred deserializing playground sport $doc in FireRepository.getPlaygroundsByIds()")
                    fireCallback(DefaultGetFireError.duringDeserialization(
                        "Error: an error occurred retrieving playgrounds info"
                    ))
                    return@addOnSuccessListener
                }

                deserializedDoc
            }

            fireCallback(Success(playgroundSportsDocs))
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred retrieving all the reservations playgrounds with ids:$ids, in FireRepository.getPlaygroundsBySportId(). Message: ${it.message}")
            fireCallback(DefaultGetFireError.default(
                "Error: an error occurred retrieving playgrounds info"
            ))
            return@addOnFailureListener
        }
}

internal fun FireRepository.getAllPlaygrounds(
    fireCallback: (FireResult<List<FirePlaygroundSport>, DefaultGetFireError>) -> Unit
) {
    // retrieve all playgrounds statically
    db.collection("playgroundSports")
        .get()
        .addOnSuccessListener { documents ->
            if (documents == null) {
                // generic error
                Log.d("generic error", "Error: retrieved null playgroundSports list in FireRepository.getAllPlaygroundsInfo()")
                fireCallback(
                    DefaultGetFireError.default(
                    "Error: playgrounds not found"
                ))
                return@addOnSuccessListener
            }

            val allPlaygroundsDocuments = mutableListOf<FirePlaygroundSport>()

            for (document in documents) {
                // deserialize playgroundSport
                val playgroundSport = FirePlaygroundSport.deserialize(document.id, document.data)

                if(playgroundSport == null){
                    // deserialization error
                    Log.d("deserialization error", "Error: deserialization error occurred for playground with id ${document.id} in FireRepository.getAllPlaygroundInfo()")
                    fireCallback(
                        DefaultGetFireError.duringDeserialization(
                        "Error: an error occurred retrieving playgrounds"
                    ))
                    return@addOnSuccessListener
                }

                allPlaygroundsDocuments.add(playgroundSport)
            }

            // * return all playground sports successfully *
            fireCallback(Success(allPlaygroundsDocuments))
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred retrieving playgrounds in FireRepository.getAllPlaygroundInfo(). Message: ${it.message}")
            fireCallback(
                DefaultGetFireError.default(
                "Error: a generic error occurred retrieving playgrounds"
            ))
        }

}

/* notifications */

internal fun FireRepository.getDynamicAllUserNotifications(
    userId: String,
    fireCallback: (FireResult<MutableList<FireNotification>, DefaultGetFireError>) -> Unit
): FireListener {
    val fireListener = FireListener()

    val notificationListener = db.collection("notifications")
        .whereEqualTo("receiverId", userId)
        .addSnapshotListener { notificationsDocuments, error ->
            if (error != null || notificationsDocuments == null) {
                // firebase error
                Log.d(
                    "generic error",
                    "Error: a generic error occurred in FireRepository.getDynamicAllUserNotifications($userId). Message: ${error?.message}"
                )
                fireCallback(
                    DefaultGetFireError.default(
                        "Error: a generic error occurred retrieving user notifications"
                    )
                )
                return@addSnapshotListener
            }

            // retrieving all the notifications related to the given user
            val fireNotifications = mutableListOf<FireNotification>()

            notificationsDocuments.forEach { document ->
                val fireNotification = FireNotification.deserialize(document.id, document.data)

                if (fireNotification == null) {
                    // deserialization error
                    Log.d(
                        "deserialization error",
                        "Error: deserialization error for document ($document) in FireRepository.getDynamicAllUserNotifications($userId)"
                    )
                    fireCallback(
                        DefaultGetFireError.duringDeserialization(
                            "Error: an error occurred retrieving user notifications"
                        )
                    )
                    return@addSnapshotListener
                }

                fireNotifications.add(fireNotification)
            }

            // * return successfully the user notifications *
            fireCallback(Success(fireNotifications))
        }

    fireListener.add(notificationListener)
    return fireListener
}

internal fun FireRepository.saveInvitation(
    notification: Notification,
    fireCallback: (FireResult<Unit, SaveAndSendInvitationFireError>) -> Unit
) {
    // convert notification entity to fireNotification
    val fireNotification = FireNotification.from(notification)

    if(fireNotification == null) {
        // conversion error
        Log.d("conversion error", "Error: an error occurred converting a notification entity in a fireNotification, in FireRepository.saveInvitation()")
        fireCallback(SaveAndSendInvitationFireError.beforeSaveAndSendPush(
            "Error: an error occurred saving the invitation"))
        return
    }

    // save notification document in the collection
    db.collection("notifications")
        .document()
        .set(fireNotification.serialize())
        .addOnSuccessListener {
            // * save was successful *
            fireCallback(Success(Unit))
        }
        .addOnFailureListener {
            // generic error
            Log.d("generic error", "Error: a generic error occurred saving invitation $notification in FireRepository.saveInvitation(). Message: ${it.message}")
            fireCallback(SaveAndSendInvitationFireError.beforeSaveAndSendPush(
                "Error: a generic error occurred saving the invitation"))
            return@addOnFailureListener
        }
}

internal fun createInvitationNotification(
    receiverToken: String,
    reservationId: String,
    notificationDescription: String,
    notificationTimestamp: String,
    fireCallback: (FireResult<Unit,SaveAndSendInvitationFireError>) -> Unit
) {
    // notification variables
    val tag = "NOTIFICATION TAG"
    val notificationTitle = "New Invitation"

    val notification = JSONObject()
    val notificationBody = JSONObject()

    try {
        // create notification body
        notificationBody.put("action", "invitation")
        notificationBody.put("title", notificationTitle)
        notificationBody.put("message", notificationDescription)
        notificationBody.put("id_reservation", reservationId)
        notificationBody.put("status", "PENDING")
        notificationBody.put("timestamp", notificationTimestamp)

        // create notification
        notification.put("to", receiverToken)
        notification.put("data", notificationBody)
    } catch (e: JSONException) {
        Log.e(tag, "createInvitationNotification function: " + e.message)

        Log.d("serialization error", "Error: a generic error occurred serializing notification JSON in FireRepository.createInvitationNotification()")
        fireCallback(SaveAndSendInvitationFireError.beforeSendPush(
            "Error: an error occurred sending the notification"
        ))
        return
    }

    // send notification
    sendInvitationNotification(notification, fireCallback)
}

private fun sendInvitationNotification(
    notification: JSONObject,
    fireCallback: (FireResult<Unit,SaveAndSendInvitationFireError>) -> Unit
) {

    // API variables
    val fcmAPI = "https://fcm.googleapis.com/fcm/send"
    val serverKey =
        "key=" + "AAAAEgeVTRw:APA91bH_I9ilwfS5o7n3U45BdKy2TQiHlBEqzbP0hONdx7IFbn1PgZdIEOk3GoMSVpQWGzKJ4so5ax50wW7hHFBuZsyVXcgp8hyM3EAqZtzSn99F5ntvV4aDht3Zl4TK5bwoWipF_9IH"
    val contentType = "application/json"

    // create request
    val request = Request.Builder()
        .url(fcmAPI)
        .post(RequestBody.create(MediaType.parse(contentType), notification.toString()))
        .addHeader("Authorization", serverKey)
        .build()

    // Send the request
    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("SEND INVITATION NOTIFICATION", "Notification sending failed! ${e.message}")
            fireCallback(SaveAndSendInvitationFireError.beforeSendPush(
                "Error: an error occurred sending the notification"
            ))
        }

        override fun onResponse(call: Call, response: Response) {
            // Handle request success
            if (response.isSuccessful) {
                Log.i("SEND INVITATION NOTIFICATION", "Notification successfully sent!")
                // * push notification successfully sent *
                fireCallback(Success(Unit))
            } else {
                Log.e("SEND INVITATION NOTIFICATION", "Notification sending failed!")
                // push notification not sent
                fireCallback(SaveAndSendInvitationFireError.beforeSendPush(
                    "Error: an error occurred sending the notification"
                ))
            }
        }
    })
}
