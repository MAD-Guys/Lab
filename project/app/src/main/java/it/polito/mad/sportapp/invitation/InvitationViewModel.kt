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
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.FireRepository
import it.polito.mad.sportapp.model.IRepository
import it.polito.mad.sportapp.model.LocalRepository
import it.polito.mad.sportapp.model.getStaticUser
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val repository: LocalRepository,
    //private val iRepository: FireRepository
) : ViewModel() {

    /**
     * TODO:
     * (1) insert iRepository in the constructor: now it raises Dagger/Hilt exceptions...
     * **/


    private var iRepository = FireRepository()

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var _loggedUser : User? = null

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

    fun getUsersFromDb(reservationId: String, sportId: String, sportName: String): FireListener {

        // set current user
        if (userId != null) {
            iRepository.getStaticUser(userId){
                when (it) {
                    is FireResult.Error -> Log.d(it.type.message(), it.errorMessage())
                    is FireResult.Success -> {
                        _loggedUser = it.value
                    }
                }
            }
        }

        //set sportId and sportName
        this.sportId = sportId
        this.sportName = sportName

        return iRepository.getAllUsersToSendInvitationTo(
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

    fun sendInvitation(receiverId: String, reservationId: String) {

        iRepository.saveAndSendInvitation(
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
                is FireResult.Error -> Log.d(it.type.message(), it.errorMessage())
                is FireResult.Success -> {/* Nothing to do */}
            }
        }

    }
}