package it.polito.mad.sportapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.IRepository
import it.polito.mad.sportapp.profile.Gender
import javax.inject.Inject

/* View Model related to the SportApp Activity */

@HiltViewModel
class SportAppViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    // notifications fire listener
    private var notificationsFireListener: FireListener? = null

    private val _isUserLoggedIn = MutableLiveData<Boolean>().also { it.value = false }
    val isUserLoggedIn: LiveData<Boolean> = _isUserLoggedIn

    internal var areVmInstancesCreated: Boolean = false

    /* notifications */
    private val _notifications =
        MutableLiveData<MutableList<Notification>>().also {

            val userId = FirebaseAuth.getInstance().currentUser?.uid

            userId?.let { uid ->
                notificationsFireListener =
                    repository.getNotificationsByUserId(uid) { notificationsResult ->
                        when (notificationsResult) {
                            is FireResult.Error -> {
                                Log.e(
                                    "getNotifications",
                                    "Error: ${notificationsResult.errorMessage()}"
                                )
                                return@getNotificationsByUserId
                            }

                            is FireResult.Success -> {
                                it.postValue(notificationsResult.value)
                            }
                        }
                    }
            }
        }
    val notifications: LiveData<MutableList<Notification>> = _notifications

    // set user logged in
    fun setUserLoggedIn(value: Boolean) {
        _isUserLoggedIn.value = value
    }

    // set vm instances created
    fun setVmInstancesCreated() {
        areVmInstancesCreated = true
    }

    /* user */
    fun checkIfUserAlreadyExists(uid: String, token: String?) {

        repository.userAlreadyExists(uid) {
            when (it) {
                is FireResult.Error -> {
                    Log.e("checkIfUserAlreadyExists", "Error: ${it.errorMessage()}")
                    return@userAlreadyExists
                }

                is FireResult.Success -> {
                    if (!it.value) {

                        // use authentication state listener to insert user data on db
                        val user = FirebaseAuth.getInstance().currentUser

                        if (user != null) {
                            val userUid = user.uid

                            val displayName = user.displayName!!.split(" ")
                            val userFirstName = displayName[0]
                            val userLastName = displayName[displayName.size - 1]
                            val userUsername = user.email!!.split("@")[0]

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
                            repository.insertNewUser(newUser) { insertResult ->
                                when (insertResult) {
                                    is FireResult.Error -> {
                                        Log.e(
                                            "insertNewUser",
                                            "Error: ${insertResult.errorMessage()}"
                                        )
                                        return@insertNewUser
                                    }

                                    is FireResult.Success -> {
                                        Log.d("insertNewUser", "Success: ${insertResult.value}")
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d("checkIfUserAlreadyExists", "User already exists")

                        // update user token
                        token?.let {
                            repository.updateUserToken(uid, token) { updateResult ->
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

    override fun onCleared() {
        super.onCleared()

        // unregister notifications fire listener
        notificationsFireListener?.unregister()
    }

}