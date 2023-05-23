package it.polito.mad.sportapp.invitation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.model.LocalRepository
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {

    private val _allUsers = MutableLiveData<MutableList<User>>()
    private val _users : MutableLiveData<MutableList<User>> = _allUsers
    val users : LiveData<MutableList<User>> = _users

    fun getUsersFromDb(sportId : Int){
        //repository.getUsersBySport(sportId)
        //TODO: this function is dummy

        // get all users from database
        val dbThread = Thread {
            val user2 = repository.getUser(2)
            val user3 = repository.getUser(3)
            val user4 = repository.getUser(4)
            val user5 = repository.getUser(5)

            _allUsers.postValue(mutableListOf(user2, user3, user4, user5))
        }

        // start db thread
        dbThread.start()

    }

    fun searchUsers(partialUsername: String) {
        //if partialUsername is "" search all
    }

    fun searchByLevel(justSelectedLevel: String?) {
        //if level is null, search everyone who selected the given sport
    }
}