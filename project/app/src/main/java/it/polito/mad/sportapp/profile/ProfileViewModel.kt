package it.polito.mad.sportapp.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.room.RoomAchievement
import it.polito.mad.sportapp.entities.room.RoomSport
import it.polito.mad.sportapp.entities.room.RoomSportLevel
import it.polito.mad.sportapp.entities.room.RoomUser
import it.polito.mad.sportapp.model.LocalRepository
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/* View Model related to the Profile Fragments */

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {

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

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

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

    // user profile pictures
    private val _userProfilePicture =
        MutableLiveData<Bitmap>().also { this.loadProfilePictureFromFirebaseStorage("profile_picture.jpeg") }
    val userProfilePicture: LiveData<Bitmap> = _userProfilePicture

    private val _userBackgroundProfilePicture =
        MutableLiveData<Bitmap>().also { this.loadProfilePictureFromFirebaseStorage("background_profile_picture.jpeg") }
    val userBackgroundProfilePicture: LiveData<Bitmap> = _userBackgroundProfilePicture

    private val _userAchievements =
        MutableLiveData<Map<RoomAchievement, Boolean>>().also { it.value = mapOf() }
    val userAchievements: LiveData<Map<RoomAchievement, Boolean>> = _userAchievements

    private val _userSports = MutableLiveData<List<RoomSportLevel>>().also { it.value = listOf() }
    val userSports: LiveData<List<RoomSportLevel>> = _userSports

    /* sports information */
    private val _sportsList = MutableLiveData<List<RoomSport>>()
    val sportsList: LiveData<List<RoomSport>> = _sportsList

    // save profile picture on firebase storage
    fun saveProfilePictureOnFirebaseStorage(
        pictureBitmap: Bitmap?,
        pictureLabel: String
    ) {

        pictureBitmap?.let {
            val pictureRef = storageRef.child("profile_pictures/$userId/$pictureLabel")

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
            }
        }
    }

    // load profile picture from firebase storage
    private fun loadProfilePictureFromFirebaseStorage(pictureLabel: String) {
        val pictureRef = storageRef.child("profile_pictures/$userId/$pictureLabel")
        val maxDownloadSize: Long = 1024 * 1024 * 10 // 10MB

        pictureRef.getBytes(maxDownloadSize).addOnSuccessListener {
            // convert byte array to bitmap
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

            when (pictureLabel) {
                "profile_picture.jpeg" -> _userProfilePicture.postValue(bitmap)
                "background_profile_picture.jpeg" -> _userBackgroundProfilePicture.postValue(bitmap)
            }
        }.addOnFailureListener {
            // load of user profile picture failed
            Log.e("ProfileViewModel", "Load of user profile picture failed!")
        }
    }

    fun loadSportsFromDb() {
        // get list of sports from database
        val dbThread = Thread {
            val sports = repository.getAllSports()
            _sportsList.postValue(sports)

            // update db flag
            _sportsListLoaded.postValue(true)
        }

        // start db thread
        dbThread.start()
    }

    // check if username is unique
    fun checkUsername(username: String) {
        val dbThread = Thread {
            val usernameAlreadyExists = repository.usernameAlreadyExists(username)
            _usernameAlreadyExists.postValue(usernameAlreadyExists)
        }
        dbThread.start()
    }

    // load user information from database
    fun loadUserInformationFromDb(userId: Int) {

        // get user information from database
        val dbThread = Thread {
            val user = repository.getUser(userId)

            // update user information
            _userFirstName.postValue(user.firstName)
            _userLastName.postValue(user.lastName)
            _userUsername.postValue(user.username)
            _userGender.postValue(user.gender)
            _userAge.postValue(user.age.toString())
            _userLocation.postValue(user.location)
            _userBio.postValue(user.bio)
            _userAchievements.postValue(user.achievements)
            _userSports.postValue(user.sportLevel)

            // update db flag
            _userSportsLoaded.postValue(true)
        }

        // start db thread
        dbThread.start()
    }

    // update user information in database
    fun updateDbUserInformation(userId: Int) {

        // set user information
        val user = RoomUser(
            userId,
            _userFirstName.value!!,
            _userLastName.value!!,
            _userUsername.value!!,
            _userGender.value!!,
            _userAge.value?.toInt()!!,
            _userLocation.value!!,
            _userBio.value!!
        )

        user.sportLevel = _userSports.value!!

        // update user information in database
        val dbThread = Thread {
            repository.updateUser(user)
        }

        // start db thread
        dbThread.start()
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

    fun setUserSports(sports: List<RoomSportLevel>) {
        _userSports.value = sports
    }

    fun setSportsInflated(value: Boolean) {
        _sportsInflated.value = value
    }

}