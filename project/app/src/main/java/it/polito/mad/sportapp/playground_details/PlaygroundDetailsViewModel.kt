package it.polito.mad.sportapp.playground_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.room.RoomPlaygroundInfo
import it.polito.mad.sportapp.entities.room.RoomReview
import it.polito.mad.sportapp.model.LocalRepository
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PlaygroundDetailsViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {

    private val _playground = MutableLiveData<RoomPlaygroundInfo?>()
    val playground : LiveData<RoomPlaygroundInfo?> = _playground

    private val _yourReview = MutableLiveData<RoomReview>()
    val yourReview : LiveData<RoomReview> = _yourReview

    private var _edit = false
    private var _tempTitle = ""
    private var _tempText = ""

    fun getPlaygroundFromDb(id : Int) {
        // get playground from database
        val dbThread = Thread {
            this._playground.postValue(repository.getPlaygroundInfoById(id))
        }

        // start db thread
        dbThread.start()
    }

    fun clearPlayground() {
        _playground.postValue(null)
    }

    fun setYourReview(){
        if(playground.value != null){
            val review = playground.value!!.reviewList.find { it.userId == 1} //TODO: change 1 with the logged user id
            _yourReview.value = review ?: RoomReview(0,1, playground.value!!.playgroundId,"",
                0f,0f,"",LocalDateTime.now().toString(),LocalDateTime.now().toString())
        }
    }

    fun updateReview(qualityRating : Float, facilitiesRating : Float, title : String, text : String){
        val updatedReview = RoomReview(
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
            repository.insertOrUpdateReview(updatedReview)
            _yourReview.postValue(repository.getReviewByUserIdAndPlaygroundId(1, _playground.value!!.playgroundId))
        }

        // start db thread
        dbThread.start()
    }

    fun updateQualityRating(r : Float){
        val updatedReview = RoomReview(
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
            repository.insertOrUpdateReview(updatedReview)
        }

        // start db thread
        dbThread.start()
    }

    fun updateFacilitiesRating(r : Float){
        val updatedReview = RoomReview(
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
            repository.insertOrUpdateReview(updatedReview)
        }

        // start db thread
        dbThread.start()
    }

    fun deleteReview(){
        val dbThread = Thread {
            repository.deleteReview(_yourReview.value!!)
        }

        // start db thread
        dbThread.start()
    }

    fun setEditMode(b : Boolean) {
        _edit = b
    }

    fun isEditMode() :Boolean {
        return _edit
    }

    fun saveEditStatus(title: String, text: String){
        _tempTitle = title
        _tempText = text
    }

    fun getTempTitle() = _tempTitle
    fun getTempText() = _tempText
    fun loggedUserCanReviewThisPlayground(): Boolean {
        return playground.value?.let { repository.loggedUserCanReviewPlayground(it.playgroundId) } ?: false
    }
}