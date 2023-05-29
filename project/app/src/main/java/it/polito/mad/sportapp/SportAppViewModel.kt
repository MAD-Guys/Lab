package it.polito.mad.sportapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.room.RoomNotification
import it.polito.mad.sportapp.entities.room.RoomNotificationStatus
import it.polito.mad.sportapp.application_utilities.checkIfUserIsLoggedIn
import it.polito.mad.sportapp.application_utilities.getCurrentUserUid
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.FireRepository
import it.polito.mad.sportapp.model.LocalRepository
import it.polito.mad.sportapp.profile.Gender
import java.time.LocalDateTime
import javax.inject.Inject

/* View Model related to the SportApp Activity */

@HiltViewModel
class SportAppViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {

    private val iRepository = FireRepository()

    /* notifications */
    private val _notifications =
        MutableLiveData<MutableList<RoomNotification>>().also { it.value = mutableListOf() }
    val notifications: LiveData<MutableList<RoomNotification>> = _notifications

    //TODO: remove the two functions below when firestore db is implemented
    fun startNotificationThread() {

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
                        "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/profile_pictures%2FfpnyG2AyUehysyf72bjov8kSc5i1%2Fprofile_picture.jpeg?alt=media&token=f7dbe2af-70fd-4e79-aea1-c29eacc2b29b",
                        RoomNotificationStatus.PENDING,
                        "@francescorosati has invited you to play a tennis match!",
                        LocalDateTime.now().toString()
                    )

                    // add a new notification every minute
                    Thread.sleep(20000)

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
    fun checkIfUserAlreadyExists(uid: String, token: String?) {

        iRepository.userAlreadyExists(uid) {
            when (it) {
                is FireResult.Error -> {
                    Log.e("checkIfUserAlreadyExists", "Error: ${it.errorMessage()}")
                    return@userAlreadyExists
                }

                is FireResult.Success -> {
                    if (!it.value) {

                        val user = FirebaseAuth.getInstance().currentUser

                        val userUid = user!!.uid

                        val displayName = user.displayName!!.split(" ")
                        val userFirstName = displayName[0]
                        val userLastName = displayName[displayName.size - 1]
                        val userUsername = user.email!!

                        // create new user
                        val newUser = User(
                            userUid,
                            userFirstName,
                            userLastName,
                            userUsername,
                            Gender.Other.name,
                            25,
                            "Turin",
                            null,
                            "Hello, I'm using EzSport!",
                            token
                        )

                        // insert user on db
                        iRepository.insertNewUser(newUser) { insertResult ->
                            when (insertResult) {
                                is FireResult.Error -> {
                                    Log.e("insertNewUser", "Error: ${insertResult.errorMessage()}")
                                    return@insertNewUser
                                }

                                is FireResult.Success -> {
                                    Log.d("insertNewUser", "Success: ${insertResult.value}")
                                }
                            }
                        }
                    } else {
                        Log.d("checkIfUserAlreadyExists", "User already exists")

                        // update user token
                        token?.let {
                            iRepository.updateUserToken(uid, token) { updateResult ->
                                when (updateResult) {
                                    is FireResult.Error -> {
                                        Log.e(
                                            "updateUserToken",
                                            "Error updating user token: ${updateResult.errorMessage()}"
                                        )
                                        return@updateUserToken
                                    }

                                    is FireResult.Success -> {
                                        Log.i(
                                            "updateUserToken",
                                            "User token with $uid, updated successfully with token: $token"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}