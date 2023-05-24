package it.polito.mad.sportapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.application_utilities.createAndSendInvitationNotification
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.NotificationStatus
import it.polito.mad.sportapp.model.LocalRepository
import java.time.LocalDateTime
import javax.inject.Inject

/* View Model related to the SportApp Activity */

@HiltViewModel
class SportAppViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {

    /* notifications */
    private val _notifications =
        MutableLiveData<MutableList<Notification>>().also { it.value = mutableListOf() }
    val notifications: LiveData<MutableList<Notification>> = _notifications

    //TODO: remove the two functions below when firestore db is implemented
    fun startNotificationThread(context: Context) {

        val thread = Thread {
            var adder = 0L

            while (true) {

                val reservationId = 11

                val notification = Notification(
                    adder.toInt(),
                    "invitation",
                    reservationId,
                    "asd423dsic9879xsdu98cs9d878dx98s7d987f98d7fg",
                    "asd423dsic9879xsdu98cs9d878dx98s7d987f98d7fg",
                    "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                    NotificationStatus.PENDING,
                    "@johndoe has invited you to play a tennis match!",
                    LocalDateTime.now().toString()
                )

                // add a new notification every two minutes
                Thread.sleep(60000)
                createAndSendInvitationNotification(
                    context,
                    reservationId,
                    notification.status,
                    notification.timestamp
                )
                _notifications.postValue(_notifications.value?.apply { add(notification) })

                adder++
            }
        }

        thread.start()
    }

    fun initializeNotificationsList() {

        _notifications.value?.add(
            Notification(
                1,
                "invitation",
                1,
                "asd423dsic9879xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsic9879xsdu98cs9dwsvdx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.PENDING,
                "@francescorosati has invited you to play a basketball match!",
                "2023-05-22T23:21:47.496"
            )
        )
        notifications.value?.add(
            Notification(
                2,
                "invitation",
                13,
                "asd423dsic9879xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsic9879xsdu98cs9dwsvdx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.PENDING,
                "@peppelazzara has invited you to play a mini-golf match!",
                "2023-05-22T17:21:47.496"
            )
        )
        notifications.value?.add(
            Notification(
                3,
                "invitation",
                15,
                "asd423dsic9sd9xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsqs9879xsdu98cs9dwsvdx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.REJECTED,
                "@peppelazzara has invited you to play a basketball match!",
                "2023-05-21T17:34:47.496"
            )
        )
        notifications.value?.add(
            Notification(
                4,
                "invitation",
                16,
                "asd423dsic9879xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsic9879xsdu98cs9dwsvdx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.CANCELED,
                "@mariomastrandrea has invited you to play a basketball match!",
                "2023-05-22T23:45:47.496"
            )
        )
        notifications.value?.add(
            Notification(
                5,
                "invitation",
                20,
                "asd423dsic9879xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsic9879xsdu98cs9dwsvdx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.ACCEPTED,
                "@mariomastrandrea has invited you to play a volleyball match!",
                "2023-05-22T13:45:47.496"
            )
        )
        notifications.value?.add(
            Notification(
                6,
                "invitation",
                23,
                "asd423dsic9879xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsic9879xsdu98cs9dwsvdx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.ACCEPTED,
                "@michelepistan has invited you to play a tennis match!",
                "2023-05-20T20:34:47.496"
            )
        )
        notifications.value?.add(
            Notification(
                7,
                "invitation",
                25,
                "asd423dsic9879xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsic9879xsdu98cs9dwsvdx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.CANCELED,
                "@michelepistan has invited you to play a 11-a-side soccer match!",
                "2023-05-03T19:20:47.496"
            )
        )
        notifications.value?.add(
            Notification(
                8,
                "invitation",
                44,
                "asd423dsic9879xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsic9879xsdu98cs9dwsvdx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.REJECTED,
                "@michelepistan has invited you to play a 8-a-side soccer match!",
                "2023-05-21T09:27:47.496"
            )
        )
        notifications.value?.add(
            Notification(
                9,
                "invitation",
                45,
                "asd423dsic9879xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsic9879xsdu98cs9dwsvdx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.ACCEPTED,
                "@johndoe has invited you to play a table tennis match!",
                "2023-05-19T11:29:47.496"
            )
        )
        notifications.value?.add(
            Notification(
                10,
                "invitation",
                46,
                "asd423dsic9879xsdu98cs9d87sdf98s7d987f98d7fg",
                "asd423dsic9879xsdu98cs9d878dx98s7d987f98d7fg",
                "https://static.vecteezy.com/ti/vettori-gratis/p1/2318271-icona-profilo-utente-vettoriale.jpg",
                NotificationStatus.ACCEPTED,
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