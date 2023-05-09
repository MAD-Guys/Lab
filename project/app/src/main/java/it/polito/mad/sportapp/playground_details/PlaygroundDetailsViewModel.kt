package it.polito.mad.sportapp.playground_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.model.Repository
import javax.inject.Inject

@HiltViewModel
class PlaygroundDetailsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _playground = MutableLiveData<PlaygroundInfo>()
    val playground : LiveData<PlaygroundInfo> = _playground

    private val _yourReview = MutableLiveData<Review>()
    val yourReview : LiveData<Review> = _yourReview

    fun getPlaygroundFromDb(id : Int) {
        // get playground from database
        val dbThread = Thread {
            this._playground.postValue(repository.getPlaygroundInfoById(id))
        }

        // start db thread
        dbThread.start()
    }

    fun setYourReview(){
        if(playground.value != null){
            // val review = Review(0,1,1,"title",5f, 3f, "review text", "", "")//playground.value.reviews.find { it.userId == 1} //TODO: change 1 with the logged user id
            _yourReview.value = Review(0,1, playground.value!!.playgroundId,"giggino title",
                5f,5f,"empty content","2023-05-04T12:04","2023-05-04T12:04")
        }
    }

    fun updateQualityRating(r : Float){
        //TODO call repository
    }

    fun updateFacilitiesRating(r : Float){
        //TODO call repository
    }
}