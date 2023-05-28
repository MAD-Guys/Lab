package it.polito.mad.sportapp.invitation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.room.RoomUser
import it.polito.mad.sportapp.model.FireRepository
import it.polito.mad.sportapp.model.IRepository
import it.polito.mad.sportapp.model.LocalRepository
import it.polito.mad.sportapp.notifications.createInvitationNotification
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val repository: LocalRepository,
    //private val iRepository: FireRepository
) : ViewModel() {

    /**
     * TODO:
     * 1) insert IRepository in the constructor: now it raises Dagger/Hilt exceptions...
     * 2) delete the commented lines, when all will be working.
     * 3) re-write sendInvitations, based on the new repository method
     * 4) retrieve the logged user id, to pass it to getAllUsersToSendInvitationTo(...)
     * **/


    private var iRepository : IRepository? = null

    init{
        iRepository = FireRepository()
    }

    /*
    private val _allUsers = MutableLiveData<MutableList<RoomUser>>()
    private val _beginnerUsers = mutableListOf<RoomUser>()
    private val _intermediateUsers = mutableListOf<RoomUser>()
    private val _expertUsers = mutableListOf<RoomUser>()
    private val _proUsers = mutableListOf<RoomUser>()
     */
    private val _allUsers = MutableLiveData<MutableList<User>>()
    private val _beginnerUsers = mutableListOf<User>()
    private val _intermediateUsers = mutableListOf<User>()
    private val _expertUsers = mutableListOf<User>()
    private val _proUsers = mutableListOf<User>()

    private var userListsInitialized = false

    private var selectedLevel: String = "All"
    private var tempPartialUsername: String = ""

    /*
    private val _users = MutableLiveData<MutableList<RoomUser>>()
    val users: LiveData<MutableList<RoomUser>> = _users
     */
    private val _users = MutableLiveData<MutableList<User>>()
    val users: LiveData<MutableList<User>> = _users

    //private var sportId = -1
    private var sportId = "x7f9jrM9BTiMoIFoyVFq"

    fun getUsersFromDb(reservationId: String, sportId: /*Int*/String): FireListener {
        //repository.getUsersBySport(sportId)
        //TODO: this function is dummy

        /*
        // get all users from database
        val dbThread = Thread {
            val user1 = repository.getUser(1)
            val user2 = repository.getUser(2)
            val user3 = repository.getUser(3)
            val user4 = repository.getUser(4)
            val user5 = repository.getUser(5)

            _allUsers.postValue(mutableListOf(user1, user2, user3, user4, user5))
            _users.postValue(mutableListOf(user1, user2, user3, user4, user5))
            userListsInitialized = false
        }

        // start db thread
        dbThread.start()
        this.sportId = sportId

         */

        //set sportId
        this.sportId = sportId

        return iRepository!!.getAllUsersToSendInvitationTo(
            "2", //TODO
            reservationId,
        ) { fireResult ->
            when (fireResult) {
                is FireResult.Success -> {
                    _allUsers.postValue(fireResult.value.toMutableList())
                    _users.postValue(fireResult.value.toMutableList())
                    userListsInitialized=false
                    initUserLists(this.sportId)
                }

                is FireResult.Error -> {

                }
            }
        }

    }

    private fun initUserLists(/*sportId: Int*/sportId: String) {
        _beginnerUsers.clear()
        _intermediateUsers.clear()
        _expertUsers.clear()
        _proUsers.clear()

        /*
        _allUsers.value?.filter { user -> user.sportLevel.any { it.sportId == sportId && it.level == "BEGINNER" } }
            ?.let { _beginnerUsers.addAll(it) }
        _allUsers.value?.filter { user -> user.sportLevel.any { it.sportId == sportId && it.level == "INTERMEDIATE" } }
            ?.let { _intermediateUsers.addAll(it) }
        _allUsers.value?.filter { user -> user.sportLevel.any { it.sportId == sportId && it.level == "EXPERT" } }
            ?.let { _expertUsers.addAll(it) }
        _allUsers.value?.filter { user -> user.sportLevel.any { it.sportId == sportId && it.level == "PRO" } }
            ?.let { _proUsers.addAll(it) }

         */

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
        //var userCollection = mutableListOf<RoomUser>()
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

    fun sendInvitation(userId: /*Int*/String, reservationId: /*Int*/String) {
    //TODO...
    /*
        createInvitationNotification(
            userId.toString() /*TODO: use the string id*/,
            reservationId,
            "" /*TODO: Agree on the message*/,
            LocalDateTime.now().toString()
        )

         */
    }
}