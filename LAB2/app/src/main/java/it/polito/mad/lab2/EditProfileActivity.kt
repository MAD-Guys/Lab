package it.polito.mad.lab2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation
import java.io.ByteArrayOutputStream
import org.json.JSONObject
import java.io.File

class EditProfileActivity : AppCompatActivity() {
    // user info fields' temporary state
    private var firstNameTemp: String? = null
    private var lastNameTemp: String? = null
    private var usernameTemp: String? = null
    private var ageTemp: String? = null
    private var radioGenderCheckedTemp = R.id.radio_male
    private var locationTemp: String? = null
    private var bioTemp: String? = null

    // User info views
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var username: EditText
    private lateinit var age: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var location: EditText
    private lateinit var bio: EditText

    // Profile picture 
    private lateinit var profilePicture: ImageView
    private lateinit var backgroundProfilePicture: ImageView
    private var profilePictureBitmap: Bitmap? = null
    private var backgroundProfilePictureBitmap: Bitmap? = null

    private var galleryUri: Uri? = null
    private var cameraUri: Uri? = null

    /* results launchers */
    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                // retrieve gallery picture Uri
                galleryUri = it.data?.data

                // convert it to a bitmap
                val galleryImageBitmap =
                    galleryUri?.let { it1 -> uriToBitmap(it1, contentResolver) }

                // edit image and show it
                editAndShowProfilePicture(galleryImageBitmap)
            }
        }

    private fun editAndShowProfilePicture(image: Bitmap?) {
        val context = this

        // crop image in the center (adapt to profile picture dimensions) and set profile picture
        Glide.with(context).asBitmap()
            .load(image)
            .override(profilePicture.width, profilePicture.height)  // set dimensions
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    croppedImage: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    // save profile picture bitmap and put it on the view
                    profilePictureBitmap = croppedImage
                    profilePicture.setImageBitmap(croppedImage)

                    // copy the profile picture to create the blurred background image
                    val backgroundImage = croppedImage.copy(croppedImage.config, true)

                    // apply blur effect to the background image after reshaping it to its view sizes
                    Glide.with(context).asBitmap()
                        .load(backgroundImage)
                        .override(backgroundProfilePicture.width, backgroundProfilePicture.height)
                        .centerCrop()
                        .transform(BlurTransformation(25))
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                blurredBackgroundImage: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                // save background image bitmap and put it on the view
                                backgroundProfilePictureBitmap = backgroundImage
                                backgroundProfilePicture.setImageBitmap(backgroundImage)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                // transform camera image uri into a (rotated) bitmap
                val cameraImageBitmap = cameraUri?.let { uri ->
                    uriToBitmap(uri, contentResolver)?.let { bitmap ->
                        rotateBitmap(cameraUri, bitmap, contentResolver)
                    }
                }

                // edit image and show it
                editAndShowProfilePicture(cameraImageBitmap)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // initialize the EditText views
        firstName = findViewById(R.id.edit_first_name)
        lastName = findViewById(R.id.edit_last_name)
        username = findViewById(R.id.edit_username)
        genderRadioGroup = findViewById(R.id.radio_gender_group)
        age = findViewById(R.id.edit_age)
        location = findViewById(R.id.edit_location)
        bio = findViewById(R.id.edit_bio)
        profilePicture = findViewById(R.id.profile_picture)
        backgroundProfilePicture = findViewById(R.id.background_profile_picture)

        // set context menu to change profile picture
        val profileImageButton: ImageButton = findViewById(R.id.profile_picture_button)
        registerForContextMenu(profileImageButton)
        profileImageButton.setOnClickListener {
            // open the related context menu
            openContextMenu(profileImageButton)
        }

        // load data from SharedPreferences and internal storage

        this.loadDataFromStorage()

        // add listeners to the temporary variables
        firstName.addTextChangedListener(textListenerInit("firstName"))
        lastName.addTextChangedListener(textListenerInit("lastName"))
        username.addTextChangedListener(textListenerInit("username"))
        age.addTextChangedListener(textListenerInit("age"))
        location.addTextChangedListener(textListenerInit("location"))
        bio.addTextChangedListener(textListenerInit("bio"))

        genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            radioGenderCheckedTemp = checkedId
        }
    }

    private fun loadDataFromStorage() {
        // retrieve data from SharedPreferences
        val sh = getSharedPreferences("it.polito.mad.lab2", MODE_PRIVATE)
        val jsonObjectProfile: JSONObject? = sh.getString("profile", null)?.let { JSONObject(it) }

        // retrieve data from the JSON object
        val firstNameResume: String =
            jsonObjectProfile?.getString("firstName") ?: getString(R.string.first_name)
        val lastNameResume: String =
            jsonObjectProfile?.getString("lastName") ?: getString(R.string.last_name)
        val usernameResume: String =
            jsonObjectProfile?.getString("username") ?: getString(R.string.username)
        val radioCheckedResume: Int = jsonObjectProfile?.getInt("radioChecked") ?: R.id.radio_male
        val ageResume: String = jsonObjectProfile?.getString("age") ?: getString(R.string.user_age)
        val locationResume: String =
            jsonObjectProfile?.getString("location") ?: getString(R.string.user_location)
        val bioResume: String = jsonObjectProfile?.getString("bio") ?: getString(R.string.user_bio)

        // retrieve profile and background picture from internal storage
        val profilePictureResume = getPictureFromInternalStorage(filesDir, "profilePicture.jpeg")
        val backgroundProfilePictureResume =
            getPictureFromInternalStorage(filesDir, "backgroundProfilePicture.jpeg")

        // set EditText views
        firstName.setText(firstNameResume)
        lastName.setText(lastNameResume)
        username.setText(usernameResume)
        genderRadioGroup.check(radioCheckedResume)
        age.setText(ageResume)
        location.setText(locationResume)
        bio.setText(bioResume)

        // set temporary variables
        firstNameTemp = firstNameResume
        lastNameTemp = lastNameResume
        usernameTemp = usernameResume
        radioGenderCheckedTemp = radioCheckedResume
        ageTemp = ageResume
        locationTemp = locationResume
        bioTemp = bioResume

        // update profile and background pictures with the one uploaded by the user, if any
        profilePictureResume?.let { profilePicture.setImageBitmap(it) }
        backgroundProfilePictureResume?.let { backgroundProfilePicture.setImageBitmap(it) }
    }

    /* save and restore temporary state */

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save temporary variables into the bundle in order
        // to have the right values once the activity restores
        outState.putString("firstNameTemp", firstNameTemp)
        outState.putString("lastNameTemp", lastNameTemp)
        outState.putString("usernameTemp", usernameTemp)
        outState.putString("ageTemp", ageTemp)
        outState.putInt("radioGenderChecked", radioGenderCheckedTemp)
        outState.putString("locationTemp", locationTemp)
        outState.putString("bioTemp", bioTemp)

        // * save pictures temporarily *

        // encode profile picture
        val encodedProfilePicture = ByteArrayOutputStream()
        profilePictureBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, encodedProfilePicture)

        // encode background picture
        val encodedBackgroundProfilePicture = ByteArrayOutputStream()
        backgroundProfilePictureBitmap?.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            encodedBackgroundProfilePicture
        )

        // save them
        outState.putByteArray("profilePictureTemp", encodedProfilePicture.toByteArray())
        outState.putByteArray(
            "backgroundProfilePictureTemp",
            encodedBackgroundProfilePicture.toByteArray()
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore temporary variables from the bundle
        firstNameTemp = savedInstanceState.getString("firstNameTemp").toString()
        firstName.setText(firstNameTemp)

        lastNameTemp = savedInstanceState.getString("lastNameTemp").toString()
        lastName.setText(lastNameTemp)

        usernameTemp = savedInstanceState.getString("usernameTemp").toString()
        username.setText(usernameTemp)

        ageTemp = savedInstanceState.getString("ageTemp").toString()
        age.setText(ageTemp)

        radioGenderCheckedTemp = savedInstanceState.getInt("radioGenderChecked")
        genderRadioGroup.check(radioGenderCheckedTemp)

        locationTemp = savedInstanceState.getString("locationTemp").toString()
        location.setText(locationTemp)

        bioTemp = savedInstanceState.getString("bioTemp").toString()
        bio.setText(bioTemp)

        // * get and show temporary images *

        // get and save profile picture
        savedInstanceState.getByteArray("profilePictureTemp")?.let {
            profilePictureBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            profilePicture.setImageBitmap(profilePictureBitmap)
        }

        // get and save background profile picture
        savedInstanceState.getByteArray("backgroundProfilePictureTemp")?.let {
            backgroundProfilePictureBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            backgroundProfilePicture.setImageBitmap(backgroundProfilePictureBitmap)
        }
    }

    private fun textListenerInit(fieldName: String): TextWatcher {
        // implement and return the TextWatcher interface
        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                when (fieldName) {
                    "firstName" -> firstNameTemp = firstName.text.toString()
                    "lastName" -> lastNameTemp = lastName.text.toString()
                    "username" -> usernameTemp = username.text.toString()
                    "age" -> ageTemp = age.text.toString()
                    "location" -> locationTemp = location.text.toString()
                    "bio" -> bioTemp = bio.text.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    /* app menu */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate and render the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_profile_menu, menu)
        // change app bar's title
        supportActionBar?.title = "Edit Profile"

        val menuHeight = supportActionBar?.height!!
        val profilePictureContainer = findViewById<ConstraintLayout>(R.id.profile_picture_container)
        val backgroundProfilePicture = findViewById<ImageView>(R.id.background_profile_picture)
        val profilePicture = findViewById<ImageView>(R.id.profile_picture)

        // set profile picture height 1/3 of the app view
        this.setProfilePictureSize(
            menuHeight,
            profilePictureContainer,
            backgroundProfilePicture,
            profilePicture
        )

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        // detect when the user clicks on the "confirm" button
        R.id.confirm_button -> {
            // if the user clicks on the confirm button, the temporary information is *serialized*
            // firstly into a JSONObject and then into the sharedPreferences file with the key *profile*
            val sh = getSharedPreferences("it.polito.mad.lab2", MODE_PRIVATE)
            val editor = sh.edit()

            val jsonObjectProfile = JSONObject()

            // serializing the temporary profile variables into the JSONObject
            jsonObjectProfile.put("firstName", firstNameTemp)
            jsonObjectProfile.put("lastName", lastNameTemp)
            jsonObjectProfile.put("username", usernameTemp)
            jsonObjectProfile.put("age", ageTemp)
            jsonObjectProfile.put("radioChecked", radioGenderCheckedTemp)
            jsonObjectProfile.put("location", locationTemp)
            jsonObjectProfile.put("bio", bioTemp)

            // manage the Gender field to display it correctly in the ShowProfileActivity
            when (radioGenderCheckedTemp) {
                R.id.radio_female -> jsonObjectProfile.put("gender", "Female")
                R.id.radio_other -> jsonObjectProfile.put("gender", "Other")
                else -> jsonObjectProfile.put("gender", "Male")
            }

            // save the profile and background pictures into the internal storage

            profilePictureBitmap?.let {
                savePictureOnInternalStorage(it, filesDir, "profilePicture.jpeg")
            }

            backgroundProfilePictureBitmap?.let {
                savePictureOnInternalStorage(it, filesDir, "backgroundProfilePicture.jpeg")
            }

            // apply changes and show a pop up to the user
            editor.putString("profile", jsonObjectProfile.toString())
            editor.apply()

            Toast.makeText(
                this,
                "Information successfully saved!", Toast.LENGTH_LONG
            ).show()

            // terminate this activity (go back to the previous one according to the stack queue)
            this.finish()
            true
        }
        // detect when the user clicks on the "back" button
        R.id.back_button -> {
            // if the user clicks the back button, the temporary information is *not* saved:
            // terminate this activity (go to the previous one according to the stack queue)
            this.finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /* context menu (to choose how to change profile picture) */

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater

        when (v.id) {
            R.id.profile_picture_button -> {
                inflater.inflate(R.menu.profile_picture_context_menu, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // detect which item has been selected by the user in the 'change profile picture' menu
        return when (item.itemId) {
            R.id.camera -> {
                // check if camera permissions have already been granted or not
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                ) {
                    // request permissions
                    val permission = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )

                    // NOTE: the request code is used to identify the request
                    // in the callback function but it is completely random
                    requestPermissions(permission, 112)
                    onRequestPermissionsResult(112, permission, intArrayOf(0, 0))
                } else openCamera()
                true
            }
            R.id.gallery -> {
                // check if gallery permissions have already been granted or not
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                ) {
                    // request permissions
                    val permission = arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )

                    requestPermissions(permission, 113)
                    onRequestPermissionsResult(113, permission, intArrayOf(0, 0))
                } else openGallery()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // if camera permissions have been granted, open the camera
        if (requestCode == 112 &&
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        )
            openCamera()
        // if gallery permissions have been granted, open the gallery
        else if (requestCode == 113 &&
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        )
            openGallery()
    }

    /* start new Activities to get a new picture */

    private fun openCamera() {
        // Creating a file object for the temporal image
        val imageFile = File.createTempFile("temp_profile_picture", ".jpeg", cacheDir)

        // Creating through a FileProvider the URI
        cameraUri = FileProvider.getUriForFile(
            this,
            "it.polito.mad.lab2.fileprovider", imageFile
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)

        cameraActivityResultLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        // create intent and open phone gallery
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(galleryIntent)
    }
}
