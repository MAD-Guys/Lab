package it.polito.mad.sportapp

import android.content.Context;
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.model.Repository
import it.polito.mad.sportapp.notifications.Notification
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/* View Model related to the SportApp Activity */

@HiltViewModel
class SportAppViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    /* notifications */
    private val _notifications =
        MutableLiveData<MutableList<Notification>>().also { it.value = mutableListOf() }
    val notifications: LiveData<MutableList<Notification>> = _notifications

    //TODO: remove the two functions below when firestore db is implemented
    fun sendNotification(context: Context) {

        val thread = Thread {
            var adder = 0L

            while (true) {

                val notification = Notification(
                    "usernameThread$adder",
                    "sportNameThread$adder",
                    "sportCenterNameThread$adder",
                    LocalDate.now().plusDays(adder),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 30)
                )

                // add a new notification every two minutes
                Thread.sleep(120000)
                _notifications.postValue(_notifications.value?.apply { add(notification) })

                adder++
            }
        }

        thread.start()
    }

    fun initializeNotificationsList() {

        val currentDate = LocalDate.now()

        _notifications.value?.add(
            Notification(
                "username1",
                "sportName1",
                "sportCenterName1",
                currentDate.plusDays(2),
                LocalTime.of(15, 0),
                LocalTime.of(16, 30)
            )
        )
        notifications.value?.add(
            Notification(
                "username2",
                "sportName2",
                "sportCenterName2",
                currentDate.plusDays(1),
                LocalTime.of(10, 30),
                LocalTime.of(12, 0)
            )
        )
        notifications.value?.add(
            Notification(
                "username3",
                "sportName3",
                "sportCenterName3",
                currentDate,
                LocalTime.of(16, 30),
                LocalTime.of(19, 0)
            )
        )
        notifications.value?.add(
            Notification(
                "username4",
                "sportName4",
                "sportCenterName4",
                currentDate.plusDays(1),
                LocalTime.of(9, 30),
                LocalTime.of(10, 0)
            )
        )
        notifications.value?.add(
            Notification(
                "username5",
                "sportName5",
                "sportCenterName5",
                currentDate.minusDays(3),
                LocalTime.of(11, 30),
                LocalTime.of(12, 30)
            )
        )
        notifications.value?.add(
            Notification(
                "username6",
                "sportName6",
                "sportCenterName6",
                currentDate.plusDays(3),
                LocalTime.of(15, 0),
                LocalTime.of(16, 30)
            )
        )
        notifications.value?.add(
            Notification(
                "username7",
                "sportName7",
                "sportCenterName7",
                currentDate.plusDays(4),
                LocalTime.of(14, 30),
                LocalTime.of(15, 30)
            )
        )
        notifications.value?.add(
            Notification(
                "username8",
                "sportName8",
                "sportCenterName8",
                currentDate.plusDays(5),
                LocalTime.of(8, 30),
                LocalTime.of(10, 0)
            )
        )
        notifications.value?.add(
            Notification(
                "username9",
                "sportName9",
                "sportCenterName9",
                currentDate.plusDays(3),
                LocalTime.of(17, 0),
                LocalTime.of(18, 30)
            )
        )
        notifications.value?.add(
            Notification(
                "username10",
                "sportName10",
                "sportCenterName10",
                currentDate.plusDays(2),
                LocalTime.of(15, 30),
                LocalTime.of(17, 0)
            )
        )
    }


    /* user */
    fun checkIfUserAlreadyExists(uid: String) {
        //TODO: setup firestore db properly and uncomment the following lines of code
        // check if user already exists in the db
        //repository.checkIfUserAlreadyExists(uid)
    }

    fun addUserOnDb() {}
}