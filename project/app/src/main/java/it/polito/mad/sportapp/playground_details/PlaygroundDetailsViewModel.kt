package it.polito.mad.sportapp.playground_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.model.Repository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
            val review = playground.value!!.reviewList.find { it.userId == 1} //TODO: change 1 with the logged user id
            _yourReview.value = review ?: Review(0,1, playground.value!!.playgroundId,"",
                0f,0f,"",LocalDateTime.now().toString(),LocalDateTime.now().toString())
        }
    }

    fun updateReview(qualityRating : Float, facilitiesRating : Float, title : String, text : String){
        val updatedReview = Review(
            _yourReview.value?.id ?: 0,
            1, //TODO: change 1 with the logged user id
            _yourReview.value?.playgroundId ?: playground.value!!.playgroundId,
            title,
            qualityRating,
            facilitiesRating,
            text,
            _yourReview.value?.timestamp ?: "",
            _yourReview.value?.lastUpdate ?: ""
        )

        val dbThread = Thread {
            repository.updateReview(updatedReview)
            _yourReview.postValue(repository.getReviewByUserIdAndPlaygroundId(1, _playground.value!!.playgroundId))
        }

        // start db thread
        dbThread.start()
    }

    fun updateQualityRating(r : Float){
        val updatedReview = Review(
            _yourReview.value?.id ?: 0,
            1, //TODO: change 1 with the logged user id
            _yourReview.value?.playgroundId ?: playground.value!!.playgroundId,
            _yourReview.value?.title ?: "",
            r,
            _yourReview.value?.facilitiesRating ?: 0f,
            _yourReview.value?.review ?: "",
            _yourReview.value?.timestamp ?: "",
            _yourReview.value?.lastUpdate ?: ""
        )

        val dbThread = Thread {
            repository.updateReview(updatedReview)
            _yourReview.postValue(repository.getReviewByUserIdAndPlaygroundId(1, _playground.value!!.playgroundId))
        }

        // start db thread
        dbThread.start()
    }

    fun updateFacilitiesRating(r : Float){
        val updatedReview = Review(
            _yourReview.value?.id ?: 0,
            1, //TODO: change 1 with the logged user id
            _yourReview.value?.playgroundId ?: playground.value!!.playgroundId,
            _yourReview.value?.title ?: "",
            _yourReview.value?.qualityRating ?: 0f,
            r,
            _yourReview.value?.review ?: "",
            _yourReview.value?.timestamp ?: "",
            _yourReview.value?.lastUpdate ?: ""
        )

        val dbThread = Thread {
            repository.updateReview(updatedReview)
            _yourReview.postValue(repository.getReviewByUserIdAndPlaygroundId(1, _playground.value!!.playgroundId))
        }

        // start db thread
        dbThread.start()
    }
}