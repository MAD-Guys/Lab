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
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultFireError
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultInsertFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.IRepository
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PlaygroundDetailsViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    private var _getError = MutableLiveData<DefaultGetFireError?>()
    val getError: LiveData<DefaultGetFireError?> = _getError

    private var _reviewError = MutableLiveData<DefaultInsertFireError?>()
    val reviewError: LiveData<DefaultInsertFireError?> = _reviewError

    private var _deleteReviewError = MutableLiveData<DefaultFireError?>()
    val deleteReviewError: LiveData<DefaultFireError?> = _deleteReviewError

    private var _reviewUpdateSuccess = MutableLiveData<String?>()
    val reviewUpdateSuccess: LiveData<String?> = _reviewUpdateSuccess

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

        return repository.getPlaygroundInfoById(id) { fireResult ->
            when (fireResult) {
                is FireResult.Error -> {
                    Log.e(fireResult.type.message(), fireResult.errorMessage())
                    _getError.postValue(fireResult.type)
                }
                is FireResult.Success -> {

                    // get playground from database
                    _playground.postValue(fireResult.value)
                }
            }
        }
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

        repository.insertOrUpdateReview(updatedReview) {
            when (it) {
                is FireResult.Error -> {
                    Log.e(it.errorType().message(), it.errorMessage())
                    _reviewError.postValue(it.type)
                }
                is FireResult.Success -> {
                    _reviewUpdateSuccess.postValue("update")
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

        repository.insertOrUpdateReview(updatedReview) {
            when (it) {
                is FireResult.Error -> {
                    Log.e(it.errorType().message(), it.errorMessage())
                    _reviewError.postValue(it.type)
                }
                is FireResult.Success -> {
                    _reviewUpdateSuccess.postValue("quality")
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

        repository.insertOrUpdateReview(updatedReview) {
            when (it) {
                is FireResult.Error -> {
                    Log.e(it.errorType().message(), it.errorMessage())
                    _reviewError.postValue(it.type)
                }
                is FireResult.Success -> {
                    _reviewUpdateSuccess.postValue("facilities")
                }
            }
        }
    }

    fun deleteReview() {
        repository.deleteReview(_yourReview.value!!) {
            when (it) {
                is FireResult.Error -> {
                    Log.e(it.errorType().message(), it.errorMessage())
                    _deleteReviewError.postValue(it.type)
                }
                is FireResult.Success -> {
                    _reviewUpdateSuccess.postValue("delete")
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
            repository.getAllEquipmentsBySportCenterIdAndSportId(
                it.sportCenterId,
                it.sportId
            ) { fireResult ->
                when (fireResult) {
                    is FireResult.Error -> {
                        Log.e(
                            fireResult.type.message(),
                            fireResult.errorMessage()
                        )
                        _getError.postValue(fireResult.type)
                    }

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

    fun clearSuccess(){
        _reviewUpdateSuccess.postValue(null)
    }

}