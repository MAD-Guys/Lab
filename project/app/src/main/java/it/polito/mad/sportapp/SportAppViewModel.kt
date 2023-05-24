package it.polito.mad.sportapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.room.RoomNotification
import it.polito.mad.sportapp.entities.room.RoomNotificationStatus
import it.polito.mad.sportapp.application_utilities.checkIfUserIsLoggedIn
import it.polito.mad.sportapp.application_utilities.getCurrentUserUid
import it.polito.mad.sportapp.model.LocalRepository
import it.polito.mad.sportapp.notifications.createInvitationNotification
import java.time.LocalDateTime
import javax.inject.Inject

/* View Model related to the SportApp Activity */

@HiltViewModel
class SportAppViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {

    /* notifications */
    private val _notifications =
        MutableLiveData<MutableList<RoomNotification>>().also { it.value = mutableListOf() }
    val notifications: LiveData<MutableList<RoomNotification>> = _notifications

    //TODO: remove the two functions below when firestore db is implemented
    fun startNotificationThread(context: Context) {

        val thread = Thread {

            var adder = 0L
            val sender = getCurrentUserUid()

            while (true) {

                if (checkIfUserIsLoggedIn()) {
                    val reservationId = 11

                    val notification = RoomNotification(
                        adder.toInt(),
                        "invitation",
                        reservationId,
                        sender ?: "",
                        "fpnyG2AyUehysyf72bjov8kSc5i1",
                        "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                        RoomNotificationStatus.PENDING,
                        "@johndoe has invited you to play a tennis match!",
                        LocalDateTime.now().toString()
                    )

                    // add a new notification every two minutes
                    Thread.sleep(60000)
                    createInvitationNotification(
                        notification.receiverUid,
                        notification.reservationId,
                        notification.status.name,
                        notification.timestamp
                    )
                    _notifications.postValue(_notifications.value?.apply { add(notification) })

                    adder++
                }
            }
        }

        thread.start()
    }

    fun initializeNotificationsList() {

        _notifications.value?.add(
            RoomNotification(
                1,
                "invitation",
                1,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.PENDING,
                "@francescorosati has invited you to play a basketball match!",
                "2023-05-22T23:21:47.496"
            )
        )
        notifications.value?.add(
            RoomNotification(
                2,
                "invitation",
                13,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.PENDING,
                "@peppelazzara has invited you to play a mini-golf match!",
                "2023-05-22T17:21:47.496"
            )
        )
        notifications.value?.add(
            RoomNotification(
                3,
                "invitation",
                15,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.REJECTED,
                "@peppelazzara has invited you to play a basketball match!",
                "2023-05-21T17:34:47.496"
            )
        )
        notifications.value?.add(
            RoomNotification(
                4,
                "invitation",
                16,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.CANCELED,
                "@mariomastrandrea has invited you to play a basketball match!",
                "2023-05-22T23:45:47.496"
            )
        )
        notifications.value?.add(
            RoomNotification(
                5,
                "invitation",
                20,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.ACCEPTED,
                "@mariomastrandrea has invited you to play a volleyball match!",
                "2023-05-22T13:45:47.496"
            )
        )
        notifications.value?.add(
            RoomNotification(
                6,
                "invitation",
                23,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.ACCEPTED,
                "@michelepistan has invited you to play a tennis match!",
                "2023-05-20T20:34:47.496"
            )
        )
        notifications.value?.add(
            RoomNotification(
                7,
                "invitation",
                25,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.CANCELED,
                "@michelepistan has invited you to play a 11-a-side soccer match!",
                "2023-05-03T19:20:47.496"
            )
        )
        notifications.value?.add(
            RoomNotification(
                8,
                "invitation",
                44,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.REJECTED,
                "@michelepistan has invited you to play a 8-a-side soccer match!",
                "2023-05-21T09:27:47.496"
            )
        )
        notifications.value?.add(
            RoomNotification(
                9,
                "invitation",
                45,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.ACCEPTED,
                "@johndoe has invited you to play a table tennis match!",
                "2023-05-19T11:29:47.496"
            )
        )
        notifications.value?.add(
            RoomNotification(
                10,
                "invitation",
                46,
                "eAXrFhgWgxccl4nmgVkgfcm5oaR2",
                "fpnyG2AyUehysyf72bjov8kSc5i1",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                RoomNotificationStatus.ACCEPTED,
                "@francescorosati has invited you to play a tennis match!",
                "2023-05-15T14:25:47.496"
            )
        )
    }


    /* user */
    fun checkIfUserAlreadyExists(uid: String) {
        //TODO: setup firestore db properly and uncomment the following lines of code
        // check if user already exists in the db
        //repository.userAlreadyExists(uid)
    }

    fun addUserOnDb() {
        //TODO: setup firestore db properly and uncomment the following lines of code
        // create user
        //repository.insertNewUser(user)
    }
}