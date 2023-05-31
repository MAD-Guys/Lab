package it.polito.mad.sportapp.invitation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.NotificationStatus
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.firestore.utilities.SaveAndSendInvitationFireError
import it.polito.mad.sportapp.model.IRepository
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var _loggedUser : User? = null

    private var _getError = MutableLiveData<DefaultGetFireError?>()
    val getError: LiveData<DefaultGetFireError?> = _getError
    private var _invitationError = MutableLiveData<SaveAndSendInvitationFireError?>()
    val invitationError: LiveData<SaveAndSendInvitationFireError?> = _invitationError
    private var _invitationSuccess = MutableLiveData<String?>()
    val invitationSuccess: LiveData<String?> = _invitationSuccess

    private val _allUsers = MutableLiveData<MutableList<User>>()
    private val _beginnerUsers = mutableListOf<User>()
    private val _intermediateUsers = mutableListOf<User>()
    private val _expertUsers = mutableListOf<User>()
    private val _proUsers = mutableListOf<User>()

    private var userListsInitialized = false

    private var selectedLevel: String = "All"
    private var tempPartialUsername: String = ""

    private val _users = MutableLiveData<MutableList<User>>()
    val users: LiveData<MutableList<User>> = _users

    //private var sportId = -1
    private var sportId = "x7f9jrM9BTiMoIFoyVFq"
    private var sportName = ""

    fun clearErrors() {
        _getError = MutableLiveData<DefaultGetFireError?>()
        _invitationError = MutableLiveData<SaveAndSendInvitationFireError?>()
    }

    fun getUsersFromDb(reservationId: String, sportId: String, sportName: String): FireListener {

        // set current user
        if (userId != null) {
            repository.getStaticUser(userId){
                when (it) {
                    is FireResult.Error -> {
                        Log.e(it.type.message(), it.errorMessage())
                        _getError.postValue(it.type)
                    }
                    is FireResult.Success -> {
                        _loggedUser = it.value
                    }
                }
            }
        }

        //set sportId and sportName
        this.sportId = sportId
        this.sportName = sportName

        return repository.getAllUsersToSendInvitationTo(
            userId!!,
            reservationId,
        ) { fireResult ->
            when (fireResult) {
                is FireResult.Success -> {
                    _allUsers.postValue(fireResult.value.toMutableList())
                    _users.postValue(fireResult.value.toMutableList())
                    userListsInitialized = false
                    initUserLists(this.sportId)
                }

                is FireResult.Error -> {
                    Log.e(fireResult.type.message(), fireResult.errorMessage())
                    _getError.postValue(fireResult.type)
                }
            }
        }
    }

    private fun initUserLists(sportId: String) {
        _beginnerUsers.clear()
        _intermediateUsers.clear()
        _expertUsers.clear()
        _proUsers.clear()

        _allUsers.value?.filter { user -> user.sportLevels.any { it.sportId == sportId && it.level == "BEGINNER" } }
            ?.let { _beginnerUsers.addAll(it) }

        _allUsers.value?.filter { user -> user.sportLevels.any { it.sportId == sportId && it.level == "INTERMEDIATE" } }
            ?.let { _intermediateUsers.addAll(it) }

        _allUsers.value?.filter { user -> user.sportLevels.any { it.sportId == sportId && it.level == "EXPERT" } }
            ?.let { _expertUsers.addAll(it) }

        _allUsers.value?.filter { user -> user.sportLevels.any { it.sportId == sportId && it.level == "PRO" } }
            ?.let { _proUsers.addAll(it) }
    }

    fun searchUsersByUsername(partialUsername: String) {

        if (!userListsInitialized) {
            initUserLists(sportId)
            userListsInitialized = true
        }

        tempPartialUsername = partialUsername
        var userCollection = mutableListOf<User>()

        when (selectedLevel) {
            "All" -> userCollection = _allUsers.value ?: mutableListOf()
            "Beginner" -> userCollection = _beginnerUsers
            "Intermediate" -> userCollection = _intermediateUsers
            "Expert" -> userCollection = _expertUsers
            "Pro" -> userCollection = _proUsers
        }

        //if partialUsername is "" search all
        if (partialUsername == "") {
            _users.postValue(userCollection)
        } else {
            val temp = userCollection.filter { it.username.contains(partialUsername, true) }
            _users.postValue(temp.toMutableList())
        }
    }

    fun searchUsersByLevel(level: String) {
        selectedLevel = level
        searchUsersByUsername(tempPartialUsername)
    }

    fun sendInvitation(receiverId: String, reservationId: String, receiverUsername: String) {

        repository.saveAndSendInvitation(
            Notification(
                null,
                "INVITATION",
                reservationId,
                userId!!,
                receiverId,
                null,
                NotificationStatus.PENDING,
                "@${_loggedUser?.username} has invited you to play a $sportName match!",
                LocalDateTime.now().toString()
            )
        ) {
            when (it) {
                is FireResult.Error -> {
                    Log.d(it.type.message(), it.errorMessage())
                    when(it.errorType()){
                        SaveAndSendInvitationFireError.NO_SAVE_AND_NO_PUSH_ERROR -> {
                            // the invitation was not saved
                            _invitationError.postValue(it.type)
                        }
                        SaveAndSendInvitationFireError.NO_PUSH_ERROR -> {
                            // the invitation was saved, but something went wrong during the sending
                            // of the push notification. Anyway, the task to invite another user was
                            // succeeded, so it could be considered as success
                            _invitationSuccess.postValue(receiverUsername)
                        }
                    }
                }
                is FireResult.Success -> {
                    _invitationSuccess.postValue(receiverUsername)
                }
            }
        }
    }

    fun clearInvitationSuccess(){
        _invitationSuccess.postValue(null)
    }
}