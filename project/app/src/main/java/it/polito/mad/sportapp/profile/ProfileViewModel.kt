package it.polito.mad.sportapp.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.SportLevel
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

/* View Model related to the Profile Fragments */

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    /* user information */

    private val _userFirstName = MutableLiveData<String>().also { it.value = "John" }
    val userFirstName: LiveData<String> = _userFirstName

    private val _userLastName = MutableLiveData<String>().also { it.value = "Doe" }
    val userLastName: LiveData<String> = _userLastName

    private val _userUsername = MutableLiveData<String>().also { it.value = "johndoe" }
    val userUsername: LiveData<String> = _userUsername

    private val _userGender = MutableLiveData<String>().also { it.value = "Male" }
    val userGender: LiveData<String> = _userGender

    private val _userAge = MutableLiveData<Int>().also { it.value = 23 }
    val userAge: LiveData<Int> = _userAge

    private val _userLocation = MutableLiveData<String>().also { it.value = "Rome" }
    val userLocation: LiveData<String> = _userLocation

    private val _userBio = MutableLiveData<String>().also {
        it.value =
            "I’m a Computer Engineering student from Latina. I love playing basketball and tennis with my friends, especially on the weekend."
    }
    val userBio: LiveData<String> = _userBio

    private val _userSports = MutableLiveData<List<SportLevel>>().also { it.value = listOf() }
    val userSports: LiveData<List<SportLevel>> = _userSports

    // load user information from database
    fun loadUserInformationFromDb(userId: Int) {

        // get user information from database
        val dbThread = Thread {
            val user = repository.getUserWithSportLevel(userId)

            // update user information
            _userFirstName.postValue(user.firstName)
            _userLastName.postValue(user.lastName)
            _userUsername.postValue(user.username)
            _userGender.postValue(user.gender)
            _userAge.postValue(user.age)
            _userLocation.postValue(user.location)
            _userBio.postValue(user.bio)
            _userSports.postValue(user.sportLevel)
        }

        // start db thread
        dbThread.start()
    }

    // update user information in database
    fun updateDbUserInformation(userId: Int) {

        // set user information
        val user = User(
            userId,
            _userFirstName.value!!,
            _userLastName.value!!,
            _userUsername.value!!,
            _userGender.value!!,
            _userAge.value!!,
            _userLocation.value!!,
            _userBio.value!!
        )

        // update user information in database
        val dbThread = Thread {
            repository.updateUser(user)
        }

        // start db thread
        dbThread.start()
    }

}