package it.polito.mad.sportapp.playground_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.sportapp.entities.DetailedPlayground
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

class PlaygroundDetailsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _playground = MutableLiveData<DetailedPlayground>()
    val playground : LiveData<DetailedPlayground> = _playground

    private val _yourReview = MutableLiveData<Any/*Review*/>()
    val yourReview : LiveData<Any/*Review*/> = _yourReview

    fun getPlaygroundFromDb(id : Int) {
        // get playground from database
        val dbThread = Thread {
            //this._playground.postValue(repository.getPlaugroundById(id))
        }

        // start db thread
        dbThread.start()
    }

    fun setYourReview(){
        if(playground.value != null){
            val review = Any()//playground.value.reviews.find { it.userId == 1} //TODO: change 1 with the logged user id
            _yourReview.value = if(review != null) review else Any() //TODO: Review(...)
        }
    }
}