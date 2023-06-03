package it.polito.mad.sportapp.notification_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.NotificationStatus
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.IRepository
import java.time.LocalDateTime
import javax.inject.Inject

/* View Model related to the Notification Details Fragment */

@HiltViewModel
class NotificationDetailsViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    // mutable live data and live data for reservation
    private val _reservation = MutableLiveData<DetailedReservation>()
    val reservation: LiveData<DetailedReservation> = _reservation

    // mutable live data and live data for notification
    private val _notification = MutableLiveData<Notification>()
    val notification: LiveData<Notification> = _notification

    private var _getError = MutableLiveData<DefaultGetFireError?>()
    var getError: LiveData<DefaultGetFireError?> = _getError

    private var _updateError = MutableLiveData<DefaultFireError?>()
    var updateError: LiveData<DefaultFireError?> = _updateError

    private val _updateSuccess = MutableLiveData<String?>()
    val updateSuccess: LiveData<String?> = _updateSuccess

    private var loggedUser : User? = null

    fun clearErrors() {
        _getError = MutableLiveData<DefaultGetFireError?>()
        getError = _getError
        _updateError = MutableLiveData<DefaultFireError?>()
        updateError = _updateError
    }

    fun getUserFromDb() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let {
            repository.getStaticUser(it) { result ->
                when (result) {
                    is FireResult.Error -> {
                        Log.e("NotificationDetailsViewModel", result.errorMessage())
                        _getError.postValue(result.type)
                    }

                    is FireResult.Success -> {
                        Log.d("NotificationDetailsViewModel", "User successfully retrieved!")
                        val returnedValue = result.value
                        loggedUser = returnedValue
                    }
                }
            }
        }
    }

    fun getNotificationFromDb(notificationId: String): FireListener {
        return repository.getNotificationById(notificationId) {
            when (it) {
                is FireResult.Error -> {
                    Log.e("NotificationDetailsViewModel", it.errorMessage())
                    _getError.postValue(it.type)
                }

                is FireResult.Success -> {
                    Log.d("NotificationDetailsViewModel", "Notification successfully retrieved!")
                    val returnedValue = it.value
                    _notification.postValue(returnedValue)
                }
            }
        }
    }

    fun getReservationFromDb(reservationId: String): FireListener {
        return repository.getDetailedReservationById(reservationId) {
            when (it) {
                is FireResult.Error -> {
                    Log.e("NotificationDetailsViewModel", it.errorMessage())
                    _getError.postValue(it.type)
                }

                is FireResult.Success -> {
                    Log.d("NotificationDetailsViewModel", "Reservation successfully retrieved!")
                    val returnedValue = it.value
                    _reservation.postValue(returnedValue)
                }
            }
        }
    }

    fun updateInvitationStatus(
        notificationId: String,
        oldStatus: NotificationStatus,
        newStatus: NotificationStatus,
        reservationId: String
    ) {
        repository.updateInvitationStatus(notificationId, oldStatus, newStatus, reservationId) {
            when (it) {
                is FireResult.Error -> {
                    Log.e("NotificationDetailsViewModel", it.errorMessage())
                    _updateError.postValue(it.type)
                }

                is FireResult.Success -> {
                    Log.d(
                        "NotificationDetailsViewModel",
                        "Notification status successfully updated!"
                    )
                    when (newStatus) {
                        NotificationStatus.ACCEPTED -> {
                            _updateSuccess.postValue("accepted")
                            sendNotificationAnswer("INVITATION_ACCEPTED")
                        }

                        NotificationStatus.REJECTED -> {
                            when (oldStatus) {
                                NotificationStatus.PENDING -> {
                                    _updateSuccess.postValue("declined")
                                    sendNotificationAnswer("INVITATION_DECLINED")
                                }

                                NotificationStatus.ACCEPTED -> {
                                    _updateSuccess.postValue("rejected")
                                    sendNotificationAnswer("INVITATION_REJECTED")
                                }

                                else -> {/* never happens */
                                }
                            }
                        }

                        else -> {/* never happens */
                        }
                    }

                }
            }
        }
    }

    private fun sendNotificationAnswer(type: String) {

        loggedUser?.let {user ->
            val notificationDescription = when(type) {
                "INVITATION_ACCEPTED" -> {
                    "@${user.username} has accepted your invitation!"
                }

                "INVITATION_DECLINED" -> {
                    "@${user.username} has declined your invitation!"
                }

                "INVITATION_REJECTED" -> {
                    "@${user.username} has rejected your invitation!"
                }

                else -> ""
            }

            val notificationAnswer = Notification(
                null,
                type,
                notification.value!!.reservationId,
                notification.value!!.receiverUid,
                notification.value!!.senderUid,
                notification.value!!.profileUrl,
                NotificationStatus.PENDING,
                notificationDescription,
                LocalDateTime.now().toString()
            )

            repository.sendPushNotificationAnswer(notificationAnswer) {
                when (it) {
                    is FireResult.Error -> {
                        Log.e("NotificationDetailsViewModel", it.errorMessage())
                    }

                    is FireResult.Success -> {
                        Log.d("NotificationDetailsViewModel", "Notification answer successfully sent!")
                    }
                }
            }
        }
    }

}