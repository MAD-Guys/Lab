package it.polito.mad.sportapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

/* View Model related to the SportApp Activity */

@HiltViewModel
class SportAppViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    fun checkIfUserAlreadyExists(uid: String) {
        //TODO: setup firestore db properly and uncomment the following lines of code
        // check if user already exists in the db
        //repository.checkIfUserAlreadyExists(uid)
    }

    fun addUserOnDb() {}
}