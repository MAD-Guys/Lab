package it.polito.mad.sportapp.notification_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.NotificationStatus
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.IRepository
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

    private val _getError = MutableLiveData<DefaultGetFireError?>()
    val getError: LiveData<DefaultGetFireError?> = _getError

    private val _updateError = MutableLiveData<DefaultFireError?>()
    val updateError: LiveData<DefaultFireError?> = _updateError

    private val _updateSuccess = MutableLiveData<String?>()
    val updateSuccess: LiveData<String?> = _updateSuccess

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
                        NotificationStatus.ACCEPTED -> _updateSuccess.postValue("accepted")
                        NotificationStatus.REJECTED -> {
                            when (oldStatus) {
                                NotificationStatus.PENDING -> _updateSuccess.postValue("declined")
                                NotificationStatus.ACCEPTED -> _updateSuccess.postValue("rejected")
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

}