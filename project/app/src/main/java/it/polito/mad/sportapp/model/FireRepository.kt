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
import it.polito.mad.sportapp.entities.firestore.FireResult
import it.polito.mad.sportapp.entities.firestore.GetItemFireError
import it.polito.mad.sportapp.entities.firestore.InsertItemFireError
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class FireRepository : IRepository {
    private val db = FirebaseFirestore.getInstance()
    override fun getUser(uid: String, fireCallback: (FireResult<User, GetItemFireError>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun userAlreadyExists(
        uid: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun usernameAlreadyExists(
        username: String,
        fireCallback: (FireResult<Boolean, DefaultFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun insertNewUser(
        user: User,
        fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit
    ) {
        // example
        // val fireUser = FireUser.from(user)
        // val serializedUser: HashMap<String,Any>? = fireUser.serialize()
        // if(serializedUser == null) {
        //   fireCallback(FireResult(InsertItemFireError.duringSerialization("sdfgh")))
        //   return
        // }
        //

        this.getReviewByUserIdAndPlaygroundId("id", "play_id") {result ->
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


        }


    }

    override fun updateUser(
        user: User,
        fireCallback: (FireResult<Unit, InsertItemFireError>) -> Unit
    ) {
        TODO("Not yet implemented")
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