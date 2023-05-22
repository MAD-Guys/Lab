package it.polito.mad.sportapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import it.polito.mad.sportapp.entities.firestore.FireResult
import it.polito.mad.sportapp.entities.firestore.FireResult.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class FirestoreRepository : IRepository {
    private val db = FirebaseFirestore.getInstance()

    override fun getUser(id: Int): LiveData<FireResult<User>> {
        val result = MutableLiveData<FireResult<User>>()

        // retrieve user asynchronously from Firestore
        db.collection("user")
            .document(id.toString())
            .addSnapshotListener { value, error ->
                if (error != null || value == null || !value.exists()) {
                    // a Firestore error occurred
                    result.value = Error("An error occurred in " +
                            "FirestoreRepository.getUser($id)\nFirestore msg: ${error?.message})"
                    )

                    return@addSnapshotListener
                }

                // convert value to User
                // val user = value.toObject(FireUser::class.java)

                // if (user == null) {
                //    result.value = Error("An error occurred transforming retrieved data to Firestore User instance, in FirestoreRepository.getUser($id))
                //    return@addSnapShopListener
                // }

                // * user data properly retrieved *

                // result.value = Success(user)
            }

        return result
    }

    override fun userAlreadyExists(uid: String): LiveData<FireResult<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun usernameAlreadyExists(username: String): LiveData<FireResult<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun insertNewUser(user: User): LiveData<FireResult<Unit>> {
        TODO("Not yet implemented")
    }

    override fun updateUser(user: User): LiveData<FireResult<Unit>> {
        TODO("Not yet implemented")
    }

    override fun getAllSports(): LiveData<FireResult<List<Sport>>> {
        TODO("Not yet implemented")
    }

    override fun getReviewByUserIdAndPlaygroundId(
        userId: Int,
        playgroundId: Int
    ): LiveData<FireResult<Review>> {
        TODO("Not yet implemented")
    }

    override fun updateReview(review: Review): LiveData<FireResult<Unit>> {
        TODO("Not yet implemented")
    }

    override fun deleteReview(review: Review): LiveData<FireResult<Unit>> {
        TODO("Not yet implemented")
    }

    override fun getDetailedReservationById(id: Int): LiveData<FireResult<DetailedReservation>> {
        TODO("Not yet implemented")
    }

    override fun overrideNewReservation(reservation: NewReservation): LiveData<FireResult<Pair<Int?, IRepository.NewReservationError?>>> {
        TODO("Not yet implemented")
    }

    override fun getReservationsPerDateByUserId(uid: String): LiveData<FireResult<Map<LocalDate, List<DetailedReservation>>>> {
        TODO("Not yet implemented")
    }

    override fun addUserToReservation(reservationId: Int, uid: String): LiveData<FireResult<Unit>> {
        TODO("Not yet implemented")
    }

    override fun getAvailableEquipmentsBySportCenterIdAndSportId(
        sportCenterId: Int,
        sportId: Int,
        reservationId: Int,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): LiveData<FireResult<MutableMap<Int, Equipment>>> {
        TODO("Not yet implemented")
    }

    override fun deleteReservation(reservation: DetailedReservation): LiveData<FireResult<Unit>> {
        TODO("Not yet implemented")
    }

    override fun getPlaygroundInfoById(playgroundId: Int): LiveData<FireResult<PlaygroundInfo>> {
        TODO("Not yet implemented")
    }

    override fun getAvailablePlaygroundsPerSlot(
        month: YearMonth,
        sport: Sport?
    ): LiveData<FireResult<MutableMap<LocalDate, MutableMap<LocalDateTime, MutableList<DetailedPlaygroundSport>>>>> {
        TODO("Not yet implemented")
    }

    override fun getAllPlaygroundsInfo(): LiveData<FireResult<List<PlaygroundInfo>>> {
        TODO("Not yet implemented")
    }

    override fun getNotificationsByUserId(uid: String): LiveData<FireResult<MutableList<Notification>>> {
        TODO("Not yet implemented")
    }

    override fun deleteNotification(notificationId: Int): LiveData<FireResult<Unit>> {
        TODO("Not yet implemented")
    }


}