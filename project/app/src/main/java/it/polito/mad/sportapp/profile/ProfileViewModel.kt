package it.polito.mad.sportapp.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import it.polito.mad.sportapp.entities.Sport
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.Achievement
import it.polito.mad.sportapp.entities.SportLevel
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.IRepository
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/* View Model related to the Profile Fragments */

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    /* user fire listener */
    private var userFireListener: FireListener = FireListener()

    /* firebase storage */
    private val storageRef = Firebase.storage("gs://sportapp-project.appspot.com/").reference

    /* sports flags */
    private val _userSportsLoaded = MutableLiveData<Boolean>().also { it.value = false }
    val userSportsLoaded: LiveData<Boolean> = _userSportsLoaded

    private val _sportsListLoaded = MutableLiveData<Boolean>().also { it.value = false }
    val sportsListLoaded: LiveData<Boolean> = _sportsListLoaded

    private val _sportsInflated = MutableLiveData<Boolean>().also { it.value = false }
    val sportsInflated: LiveData<Boolean> = _sportsInflated

    /* user information */

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val _usernameAlreadyExists = MutableLiveData<Boolean>().also { it.value = false }
    val usernameAlreadyExists: LiveData<Boolean> = _usernameAlreadyExists

    private val _userFirstName = MutableLiveData<String>().also { it.value = "John" }
    val userFirstName: LiveData<String> = _userFirstName

    private val _userLastName = MutableLiveData<String>().also { it.value = "Doe" }
    val userLastName: LiveData<String> = _userLastName

    private val _userUsername = MutableLiveData<String>().also { it.value = "johndoe" }
    val userUsername: LiveData<String> = _userUsername

    private val _userGender = MutableLiveData<String>().also { it.value = "Male" }
    val userGender: LiveData<String> = _userGender

    private val _userAge = MutableLiveData<String>().also { it.value = 25.toString() }
    val userAge: LiveData<String> = _userAge

    private val _userLocation = MutableLiveData<String>().also { it.value = "Rome" }
    val userLocation: LiveData<String> = _userLocation

    private val _userBio = MutableLiveData<String>().also {
        it.value =
            "Hello, I'm using EzSport!"
    }
    val userBio: LiveData<String> = _userBio

    private var userNotificationsToken: String? = null
    private var userImageUrl: String? = null

    // user profile pictures
    private val _userProfilePicture =
        MutableLiveData<Bitmap>().also {
            this.loadProfilePictureFromFirebaseStorage("profile_picture.jpeg")
        }
    val userProfilePicture: LiveData<Bitmap> = _userProfilePicture

    private val _userBackgroundProfilePicture =
        MutableLiveData<Bitmap>().also {
            this.loadProfilePictureFromFirebaseStorage("background_profile_picture.jpeg")
        }
    val userBackgroundProfilePicture: LiveData<Bitmap> = _userBackgroundProfilePicture

    private val _userAchievements =
        MutableLiveData<Map<Achievement, Boolean>>().also { it.value = mapOf() }
    val userAchievements: LiveData<Map<Achievement, Boolean>> = _userAchievements

    private val _userSports = MutableLiveData<List<SportLevel>>().also { it.value = listOf() }
    val userSports: LiveData<List<SportLevel>> = _userSports

    /* sports information */
    private val _sportsList = MutableLiveData<List<Sport>>().also { list ->
        repository.getAllSports { newSportsResult ->
            when (newSportsResult) {
                is FireResult.Success -> {
                    list.postValue(newSportsResult.value)

                    Log.d("ProfileViewModel", "Sports list successfully loaded!")

                    // update db flag
                    _sportsListLoaded.postValue(true)
                }

                is FireResult.Error -> {
                    Log.e("ProfileViewModel", "Error while loading sports list!")
                }
            }
        }
    }
    val sportsList: LiveData<List<Sport>> = _sportsList

    // init block
    init {

        // load user information from firestore db
        userId?.let { uid ->
            userFireListener = loadUserInformationFromDb(uid)
        }
    }

    // save profile picture on firebase storage
    fun saveProfilePictureOnFirebaseStorage(
        pictureBitmap: Bitmap?,
        pictureLabel: String
    ) {

        userId?.let { uid ->
            pictureBitmap?.let {

                // set new profile picture bitmap
                when (pictureLabel) {
                    "profile_picture.jpeg" -> _userProfilePicture.postValue(pictureBitmap)
                    "background_profile_picture.jpeg" -> _userBackgroundProfilePicture.postValue(
                        pictureBitmap
                    )
                }

                val pictureRef = storageRef.child("profile_pictures/$uid/$pictureLabel")

                // convert bitmap to byte array
                val bytes = ByteArrayOutputStream()
                pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

                // upload byte array to firebase storage
                val uploadTask = pictureRef.putStream(bytes.toByteArray().inputStream())

                uploadTask.addOnFailureListener {
                    // upload of user profile picture failed
                    Log.e("ProfileViewModel", "Upload of user profile picture failed!")
                }.addOnSuccessListener {
                    // upload of user profile picture succeeded
                    Log.d("ProfileViewModel", "Upload of user profile picture succeeded!")

                    // get picture url
                    pictureRef.downloadUrl.addOnSuccessListener { uri ->
                        // load of user profile picture succeeded
                        Log.i(
                            "ProfileViewModel",
                            "Download of user profile picture succeeded! URL = $uri"
                        )

                        // set new profile picture bitmap
                        when (pictureLabel) {
                            "profile_picture.jpeg" -> updateUserProfileUrl(uri.toString())
                        }
                    }
                }
            }
        }
    }

    // load profile picture from firebase storage
    private fun loadProfilePictureFromFirebaseStorage(pictureLabel: String) {

        userId?.let { uid ->
            val pictureRef = storageRef.child("profile_pictures/$uid/$pictureLabel")
            val maxDownloadSize: Long = 1024 * 1024 * 10 // 10MB

            pictureRef.getBytes(maxDownloadSize).addOnSuccessListener {
                // convert byte array to bitmap
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

                when (pictureLabel) {
                    "profile_picture.jpeg" -> _userProfilePicture.postValue(bitmap)
                    "background_profile_picture.jpeg" -> _userBackgroundProfilePicture.postValue(
                        bitmap
                    )
                }
            }.addOnFailureListener {
                // load of user profile picture failed
                Log.e("ProfileViewModel", "Load of user profile picture failed!")
            }
        }
    }

    // update url of the user profile picture
    private fun updateUserProfileUrl(pictureUrl: String) {

        userId?.let { uid ->
            repository.updateUserImageUrl(uid, pictureUrl) {
                when (it) {
                    is FireResult.Error -> {
                        Log.e("ProfileViewModel", "Error updating user profile picture url!")
                        return@updateUserImageUrl
                    }

                    is FireResult.Success -> {
                        Log.d("ProfileViewModel", "User profile picture url updated successfully!")
                    }
                }
            }
        }
    }

    // check if username is unique
    fun checkUsername(username: String) {

        repository.usernameAlreadyExists(username) {
            when (it) {
                is FireResult.Success -> {
                    _usernameAlreadyExists.postValue(it.value)
                }

                is FireResult.Error -> {
                    Log.e("ProfileViewModel", "Error while checking username!")
                }
            }
        }
    }

    // load user information from database
    private fun loadUserInformationFromDb(uid: String): FireListener {

        // get user information from database
        return repository.getUserWithAchievements(uid) { newUser ->
            when (newUser) {
                is FireResult.Success -> {
                    // user information successfully loaded
                    Log.d("ProfileViewModel", "User information successfully loaded!")

                    // update user information
                    _userFirstName.postValue(newUser.value.firstName)
                    _userLastName.postValue(newUser.value.lastName)
                    _userUsername.postValue(newUser.value.username)
                    _userGender.postValue(newUser.value.gender)
                    _userAge.postValue(newUser.value.age.toString())
                    _userLocation.postValue(newUser.value.location)
                    _userBio.postValue(newUser.value.bio)
                    _userAchievements.postValue(newUser.value.achievements)
                    _userSports.postValue(newUser.value.sportLevels)

                    userImageUrl = newUser.value.imageURL
                    userNotificationsToken = newUser.value.notificationsToken

                    // update db flag
                    _userSportsLoaded.postValue(true)
                }

                is FireResult.Error -> {
                    // error while loading user information
                    Log.e("ProfileViewModel", "Error while loading user information!")
                    //showToasty("error", context, newUser.errorMessage())
                }
            }
        }
    }

    // update user information in database
    fun updateDbUserInformation() {

        // set user information
        val user = User(
            userId,
            _userFirstName.value!!,
            _userLastName.value!!,
            _userUsername.value!!,
            _userGender.value!!,
            _userAge.value?.toInt()!!,
            _userLocation.value!!,
            userImageUrl,
            _userBio.value!!,
            userNotificationsToken
        )

        user.sportLevels = _userSports.value!!

        userId?.let {
            repository.updateUser(user) {
                when (it) {
                    is FireResult.Success -> {
                        // user information successfully updated
                        Log.d("ProfileViewModel", "User information successfully updated!")
                        //showToasty("success", context, "Information successfully updated!")
                    }

                    is FireResult.Error -> {
                        // error while updating user information
                        Log.e("ProfileViewModel", "Error while updating user information!")
                        //showToasty("error", context, it.errorMessage())
                    }
                }
            }
        }
    }

    /* user information setters */
    fun setUserFirstName(firstName: String) {
        _userFirstName.value = firstName
    }

    fun setUserLastName(lastName: String) {
        _userLastName.value = lastName
    }

    fun setUserUsername(username: String) {
        _userUsername.value = username
    }

    fun setUserGender(gender: String) {
        _userGender.value = gender
    }

    fun setUserAge(age: String) {
        _userAge.value = age
    }

    fun setUserLocation(location: String) {
        _userLocation.value = location
    }

    fun setUserBio(bio: String) {
        _userBio.value = bio
    }

    fun setUserSports(sports: List<SportLevel>) {
        _userSports.value = sports
    }

    fun setSportsInflated(value: Boolean) {
        _sportsInflated.value = value
    }

    override fun onCleared() {
        super.onCleared()

        // unregister from user listener
        userFireListener.unregister()
    }

}