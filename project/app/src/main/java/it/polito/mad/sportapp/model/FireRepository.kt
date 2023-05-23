package it.polito.mad.sportapp.model

import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.FireErrorType
import it.polito.mad.sportapp.entities.firestore.FireResult
import it.polito.mad.sportapp.entities.firestore.FireUser
import it.polito.mad.sportapp.entities.firestore.GetItemFireError
import it.polito.mad.sportapp.entities.firestore.InsertItemFireError
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class FireRepository : IRepository {
    private val db = FirebaseFirestore.getInstance()
    override fun getUser(uid: String, fireCallback: (FireResult<User, GetItemFireError>) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fireUser = FireUser.deserialize(document.data!!.toMutableMap())
                    if (fireUser != null) {
                        fireCallback(FireResult.Success(fireUser.to()))
                    } else {
                        fireCallback(FireResult.Error(GetItemFireError.DEFAULT_FIRE_ERROR))
                    }
                } else {
                    fireCallback(FireResult.Error(GetItemFireError.NOT_FOUND_ERROR))
                }
            }
            .addOnFailureListener {
                fireCallback(FireResult.Error(GetItemFireError.DEFAULT_FIRE_ERROR))
            }
    }

    override fun userAlreadyExists(
        uid: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    ) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    fireCallback(FireResult.Success(true))
                } else {
                    fireCallback(FireResult.Success(false))
                }
            }
            .addOnFailureListener {
                fireCallback(FireResult.Error(DefaultFireError("Error while checking user")))
            }
    }

    override fun usernameAlreadyExists(
        username: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    ) {
        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    fireCallback(FireResult.Success(false))
                } else {
                    fireCallback(FireResult.Success(true))
                }
            }
            .addOnFailureListener {
                fireCallback(FireResult.Error(DefaultFireError("Error while checking username")))
            }
    }

    override fun insertNewUser(
        user: User,
        fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit
    ) {
        val fireUser = FireUser.from(user)
        val serializedUser: Map<String,Any>? = fireUser.serialize()
        if(serializedUser == null) {
        fireCallback(FireResult.Error(InsertItemFireError.SERIALIZATION_ERROR))
          return
         }
        else {
            db.collection("users").document().set(serializedUser)
                .addOnSuccessListener {
                    fireCallback(FireResult.Success(Unit))
                }
                .addOnFailureListener {
                    fireCallback(FireResult.Error(InsertItemFireError.DEFAULT_FIRE_ERROR))
                }
            return
        }
        //

        /*this.getReviewByUserIdAndPlaygroundId("id", "play_id") {result ->
            if(result.isError()) {
                val errType = result.errorType()
                errType.message()
            }else {

                val review = result.unwrap()
            }

            when(result) {
                is FireResult.Error -> {
                    val errorType = result.type
                }
                is FireResult.Success -> {
                    val value = result.value
                }
            }


        }*/


    }

    override fun updateUser(
        user: User,
        fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit
    ) {
        val fireUser = FireUser.from(user)
        val serializedUser: Map<String,Any>? = fireUser.serialize()
        if(serializedUser == null) {
            fireCallback(FireResult.Error(InsertItemFireError.SERIALIZATION_ERROR))
            return
        }
        else {
            db.collection("users").document(fireUser.uid).set(serializedUser)
                .addOnSuccessListener {
                    fireCallback(FireResult.Success(Unit))
                }
                .addOnFailureListener {
                    fireCallback(FireResult.Error(InsertItemFireError.DEFAULT_FIRE_ERROR))
                }
            return
        }
    }

    override fun getAllSports(fireCallback: (FireResult<List<Sport>, DefaultFireError>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getReviewByUserIdAndPlaygroundId(
        uid: String,
        playgroundId: String,
        fireCallback: (FireResult<Review, GetItemFireError>) -> Unit
    ) {
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

    override fun getDetailedReservationById(
        reservationId: String,
        fireCallback: (FireResult<DetailedReservation, GetItemFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun overrideNewReservation(
        reservation: NewReservation,
        fireCallback: (FireResult<Int, IRepository.NewReservationError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getReservationsPerDateByUserId(
        uid: String,
        fireCallback: (FireResult<Map<LocalDate, List<DetailedReservation>>, GetItemFireError>) -> Unit
    ) {
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
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteReservation(
        reservation: DetailedReservation,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getPlaygroundInfoById(
        playgroundId: String,
        fireCallback: (FireResult<PlaygroundInfo, GetItemFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getAvailablePlaygroundsPerSlot(
        month: YearMonth,
        sport: Sport?,
        fireCallback: (FireResult<MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>>, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllPlaygroundsInfo(fireCallback: (FireResult<List<PlaygroundInfo>, DefaultFireError>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getNotificationsByUserId(
        uid: String,
        fireCallback: (FireResult<MutableList<Notification>, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteNotification(
        notificationId: String,
        fireCallback: (FireResult<Unit, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }


}