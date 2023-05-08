package it.polito.mad.sportapp.playground_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.sportapp.entities.DetailedPlayground
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

class PlaygroundDetailsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _playground = MutableLiveData<DetailedPlayground>()
    val playground : LiveData<DetailedPlayground> = _playground

    private val _yourReview = MutableLiveData<Review>()
    val yourReview : LiveData<Review> = _yourReview

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
            val review = Review(0,1,1,"title",5f, 3f, "review text", "", "")//playground.value.reviews.find { it.userId == 1} //TODO: change 1 with the logged user id
            _yourReview.value = if(review != null) review else Review(0,1,1,"",0f,0f,"","","")
        }
    }
}