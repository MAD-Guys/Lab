package it.polito.mad.sportapp.invitation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.room.RoomUser
import it.polito.mad.sportapp.model.LocalRepository
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {

    private val _allUsers = MutableLiveData<MutableList<RoomUser>>()
    private val _users = MutableLiveData<MutableList<RoomUser>>()
    val users : LiveData<MutableList<RoomUser>> = _users

    private var sportId = -1

    fun getUsersFromDb(sportId : Int){
        //repository.getUsersBySport(sportId)
        //TODO: this function is dummy

        // get all users from database
        val dbThread = Thread {
            val user1 = repository.getUser(1)
            val user2 = repository.getUser(2)
            val user3 = repository.getUser(3)
            val user4 = repository.getUser(4)
            val user5 = repository.getUser(5)

            _allUsers.postValue(mutableListOf(user1, user2, user3, user4, user5))
            _users.postValue(mutableListOf(user1, user2, user3, user4, user5))
        }

        // start db thread
        dbThread.start()
        this.sportId = sportId

    }

    fun searchUsersByUsername(partialUsername: String) {
        //if partialUsername is "" search all
        if(partialUsername == ""){
            _users.postValue(_allUsers.value)
        } else {
            val temp = _allUsers.value?.filter { it.username.contains(partialUsername, true) }
            _users.postValue(temp?.toMutableList())
        }
    }

    fun searchUsersByLevel(level: String) {
        if(level == "All"){
            _users.postValue(_allUsers.value)
        } else {
            val temp = _allUsers.value?.filter { user -> user.sportLevel.any { it.sportId == sportId && it.level == level.uppercase() } }
            _users.postValue(temp?.toMutableList())
        }
    }

    fun sendInvitation(userId: Int) {
        //TODO
    }
}