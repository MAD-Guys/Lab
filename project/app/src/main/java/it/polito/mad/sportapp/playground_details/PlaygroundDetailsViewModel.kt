package it.polito.mad.sportapp.playground_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.Review
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.entities.room.RoomEquipment
import it.polito.mad.sportapp.entities.room.RoomPlaygroundInfo
import it.polito.mad.sportapp.entities.room.RoomReview
import it.polito.mad.sportapp.model.FireRepository
import it.polito.mad.sportapp.model.LocalRepository
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PlaygroundDetailsViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {

    private val iRepository = FireRepository()

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val _playground = MutableLiveData<PlaygroundInfo?>()
    val playground: LiveData<PlaygroundInfo?> = _playground

    private val _yourReview = MutableLiveData<Review>()
    val yourReview: LiveData<Review> = _yourReview

    private val _equipments = MutableLiveData<List<Equipment>?>()
    val equipments: LiveData<List<Equipment>?> = _equipments

    private var _edit = false
    private var _tempTitle = ""
    private var _tempText = ""
    private var _loggedUserCanReviewThisPlayground = MutableLiveData<Boolean?>()
    val loggedUserCanReviewThisPlayground: LiveData<Boolean?> = _loggedUserCanReviewThisPlayground

    fun getPlaygroundFromDb(id: String): FireListener {

        return iRepository.getPlaygroundInfoById(id) { fireResult ->
            when (fireResult) {
                is FireResult.Error -> Log.d(fireResult.type.message(), fireResult.errorMessage())
                is FireResult.Success -> {

                    // get playground from database
                    _playground.postValue(fireResult.value)
                }
            }
        }
    }

    fun clearPlayground() {
        _playground.postValue(null)
    }

    fun setYourReview() {
        if (playground.value != null) {
            val review =
                playground.value!!.reviewList.find { it.userId == userId!! }
            _yourReview.value = review ?: Review(
                null, userId!!, playground.value!!.playgroundId, "",
                0f, 0f, "", LocalDateTime.now().toString(), LocalDateTime.now().toString()
            )
        }
    }

    fun updateReview(qualityRating: Float, facilitiesRating: Float, title: String, text: String) {
        val updatedReview = Review(
            _yourReview.value?.id,
            userId!!,
            _yourReview.value?.playgroundId ?: playground.value!!.playgroundId,
            title,
            qualityRating,
            facilitiesRating,
            text,
            _yourReview.value?.timestamp ?: "",
            _yourReview.value?.lastUpdate ?: ""
        )

        iRepository.insertOrUpdateReview(updatedReview) {
            when (it) {
                is FireResult.Error -> Log.d(it.errorType().message(), it.errorMessage())
                is FireResult.Success -> {/* Nothing to do */
                }
            }
        }
    }

    fun updateQualityRating(r: Float) {
        val updatedReview = Review(
            _yourReview.value?.id,
            userId!!,
            _yourReview.value?.playgroundId ?: playground.value!!.playgroundId,
            _yourReview.value?.title ?: "",
            r,
            _yourReview.value?.facilitiesRating ?: 0f,
            _yourReview.value?.review ?: "",
            _yourReview.value?.timestamp ?: "",
            _yourReview.value?.lastUpdate ?: ""
        )

        iRepository.insertOrUpdateReview(updatedReview) {
            when (it) {
                is FireResult.Error -> Log.d(it.errorType().message(), it.errorMessage())
                is FireResult.Success -> {/* Nothing to do */
                }
            }
        }
    }

    fun updateFacilitiesRating(r: Float) {
        val updatedReview = Review(
            _yourReview.value?.id,
            userId!!,
            _yourReview.value?.playgroundId ?: playground.value!!.playgroundId,
            _yourReview.value?.title ?: "",
            _yourReview.value?.qualityRating ?: 0f,
            r,
            _yourReview.value?.review ?: "",
            _yourReview.value?.timestamp ?: "",
            _yourReview.value?.lastUpdate ?: ""
        )

        iRepository.insertOrUpdateReview(updatedReview) {
            when (it) {
                is FireResult.Error -> Log.d(it.errorType().message(), it.errorMessage())
                is FireResult.Success -> {/* Nothing to do */
                }
            }
        }
    }

    fun deleteReview() {
        iRepository.deleteReview(_yourReview.value!!) {
            when (it) {
                is FireResult.Error -> Log.d(it.errorType().message(), it.errorMessage())
                is FireResult.Success -> {/* Nothing to do */
                }
            }
        }
    }

    fun setEditMode(b: Boolean) {
        _edit = b
    }

    fun isEditMode(): Boolean {
        return _edit
    }

    fun saveEditStatus(title: String, text: String) {
        _tempTitle = title
        _tempText = text
    }

    fun getTempTitle() = _tempTitle
    fun getTempText() = _tempText

    /*
    fun loggedUserCanReviewThisPlayground(): Boolean {
        return _loggedUserCanReviewThisPlayground
    }

     */

    fun loadEquipmentsFromDb() {
        _playground.value?.let {
            iRepository.getAllEquipmentsBySportCenterIdAndSportId(
                it.sportCenterId,
                it.sportId
            ) { fireResult ->
                when (fireResult) {
                    is FireResult.Error -> Log.d(
                        fireResult.type.message(),
                        fireResult.errorMessage()
                    )

                    is FireResult.Success -> {
                        _equipments.postValue(fireResult.value)
                    }
                }
            }
        }
    }

    fun setLoggedUserCanReviewThisPlayground() {

        _loggedUserCanReviewThisPlayground.postValue(true)

        /*
        // ask if the current user can review
        playground.value?.let {
            iRepository.loggedUserCanReviewPlayground(
                userId!!,
                it.playgroundId
            ) { it ->
                when (it) {
                    is FireResult.Error -> Log.e(
                        it.errorType().message(),
                        it.errorMessage()
                    )

                    is FireResult.Success -> _loggedUserCanReviewThisPlayground.postValue(it.value)

                }
            }
        }

         */
    }

}