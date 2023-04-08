package it.polito.mad.lab2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import es.dmoral.toasty.Toasty

class EditProfileActivity : AppCompatActivity() {
    // user info fields' temporary state
    internal var firstNameTemp: String? = null
    internal var lastNameTemp: String? = null
    internal var usernameTemp: String? = null
    internal var ageTemp: String? = null
    internal var radioGenderCheckedTemp: Gender? = null
    internal var locationTemp: String? = null
    internal var bioTemp: String? = null

    // Sports temporary state
    internal var sportsTemp = mutableMapOf(
        // 10 values initially set according to the values hard coded in ShowProfileActivity.kt
        Pair("basket",      Sport("basket",     false, Level.NO_LEVEL)),
        Pair("soccer11",    Sport("soccer11",   false, Level.NO_LEVEL)),
        Pair("soccer5",     Sport("soccer5",    false, Level.NO_LEVEL)),
        Pair("soccer8",     Sport("soccer8",    false, Level.NO_LEVEL)),
        Pair("tennis",      Sport("tennis",     false, Level.NO_LEVEL)),
        Pair("tableTennis", Sport("tableTennis",false, Level.NO_LEVEL)),
        Pair("volleyball",  Sport("volleyball", false, Level.NO_LEVEL)),
        Pair("beachVolley", Sport("beachVolley",false, Level.NO_LEVEL)),
        Pair("padel",       Sport("padel",      false, Level.NO_LEVEL)),
        Pair("miniGolf",    Sport("miniGolf",   false, Level.NO_LEVEL))
    )

    // User info views
    internal lateinit var firstName: EditText
    internal lateinit var lastName: EditText
    internal lateinit var username: EditText
    internal lateinit var age: EditText
    internal lateinit var genderRadioGroup: RadioGroup
    internal lateinit var location: EditText
    internal lateinit var bio: EditText

    // Sports views: each element contains the Sport Chip,
    //               the levels ChipGroup and a list of the levels' Chips
    internal val sports = HashMap<String,SportChips>()

    // Profile picture
    internal lateinit var profilePicture: ImageView
    internal lateinit var backgroundProfilePicture: ImageView
    internal var profilePictureBitmap: Bitmap? = null
    internal var backgroundProfilePictureBitmap: Bitmap? = null

    internal var cameraUri: Uri? = null
    private lateinit var cropImageOptions: CropImageOptions

    /* results launchers */
    internal var galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                // retrieve gallery picture Uri
                val galleryUri = it.data?.data

                cropPicture.launch(
                    CropImageContractOptions(
                        galleryUri,
                        cropImageOptions
                    )
                )
            }
        }

    internal var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {

                cropPicture.launch(
                    CropImageContractOptions(
                        cameraUri,
                        cropImageOptions
                    )
                )
            }
        }

    private val cropPicture = registerForActivityResult(CropImageContract()) {
        if (it.isSuccessful) {
            val cropUri = it.uriContent

            // convert cropUri to a bitmap and rotate it
            profilePictureBitmap = cropUri?.let { uri ->
                uriToBitmap(uri, contentResolver)?.let { bitmap ->
                    rotateBitmap(uri, bitmap, contentResolver)
                }
            }

            // set profile picture
            profilePicture.setImageBitmap(profilePictureBitmap)

            // copy the profile picture to create the blurred background image
            val backgroundPicture = profilePictureBitmap?.copy(profilePictureBitmap!!.config, true)

            backgroundPicture?.let { bitmap ->

                // blur the background picture
                backgroundProfilePictureBitmap = fastblur(bitmap, 0.5f, 25)

                // set background profile picture
                backgroundProfilePicture.setImageBitmap(backgroundProfilePictureBitmap)
            }

        } else {
            Log.d("CROP", "Cropping failed: ${it.error}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // configure toasts appearance
        Toasty.Config.getInstance()
            .allowQueue(true) // optional (prevents several Toastys from queuing)
            .setGravity(
                Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                0,
                100
            ) // optional (set toast gravity, offsets are optional)
            .supportDarkTheme(true) // optional (whether to support dark theme or not)
            .setRTL(true) // optional (icon is on the right)
            .apply() // required

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

        // initialize options to crop the profile picture
        cropImageOptions = CropImageOptions(
            guidelines = CropImageView.Guidelines.ON,
            outputCompressFormat = Bitmap.CompressFormat.JPEG,
            outputCompressQuality = 100,
            autoZoomEnabled = true,
            allowFlipping = false,
            allowRotation = false,
            fixAspectRatio = true,
            aspectRatioX = 1,
            aspectRatioY = 1
        )

        // fills the sportChips and sportLevels lists
        this.sportsInit()

        /* manage listeners */

        // set context menu to change profile picture
        val profileImageButton: ImageButton = findViewById(R.id.profile_picture_button)
        registerForContextMenu(profileImageButton)
        profileImageButton.setOnClickListener {
            // open the related context menu
            openContextMenu(profileImageButton)
        }



        // add listeners to the user info views
        firstName.addTextChangedListener(textListenerInit("firstName"))
        lastName.addTextChangedListener(textListenerInit("lastName"))
        username.addTextChangedListener(textListenerInit("username"))
        age.addTextChangedListener(textListenerInit("age"))
        location.addTextChangedListener(textListenerInit("location"))
        bio.addTextChangedListener(textListenerInit("bio"))

        genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            radioGenderCheckedTemp = when(checkedId) {
                R.id.radio_male -> Gender.Male
                R.id.radio_female -> Gender.Female
                R.id.radio_other -> Gender.Other
                else -> throw RuntimeException("An unexpected Gender Button id has been detected")
            }
        }

        // add listeners to the sports views
        for ((sportName, sportChips) in sports) {
            // add listener to the sport chip
            sportChips.chip.setOnClickListener { sportChipListener(sportName) }

            // add listener to the sport levels chips
            sportChips.levelsChipGroup.setOnCheckedStateChangeListener { it, _ ->
                sportLevelListener(it, sportName)
            }

            // add listener to the actual level chip
            sportChips.actualLevelChip.setOnClickListener {
                // toggle sport levels chip group
                toggleSportLevelsVisibility(sportName)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // load data from SharedPreferences and internal storage
        this.loadDataFromStorage()
        //val sportsContainer = findViewById<ChipGroup>(R.id.sports_container)
        //sportsContainer.requestLayout()
    }

    override fun onPause() {
        super.onPause()

        // save the temporary profile variables into the sharedPreferences file
        saveInformationOnStorage()
    }

    override fun onDestroy() {
        super.onDestroy()

        // the clearStorageFile function is called here because
        // the cache must be cleared before the EditProfileActivity ends but
        // cannot be cleared before the picture is cropped

        // delete the temporary profile pictures saved into cache (if any)
        clearStorageFiles(cacheDir, "temp_profile_picture[a-zA-Z0-9]*.jpeg")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // show a pop up to the user when coming back to the previous activity
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showToasty("success", this, "Information correctly saved!")
        }

        return super.onKeyDown(keyCode, event)
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
            // (the information will be persistently saved in the onPause method)

            // showing feedback information
            showToasty("success", this, "Information correctly saved!")

            // terminate this activity (go back to the previous one according to the stack queue)
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
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    // request permissions
                    val permission = arrayOf(
                        Manifest.permission.CAMERA
                    )

                    // NOTE: the request code is used to identify the request
                    // in the callback function but it is completely random
                    ActivityCompat.requestPermissions(this, permission, 112)
                }
                else openCamera()
                true
            }
            R.id.gallery -> {
                // check if gallery permissions have already been granted or not
                if (ActivityCompat.checkSelfPermission(this, galleryImagesPermission()) == PackageManager.PERMISSION_DENIED) {
                    // request permissions
                    val permissions = arrayOf(
                        galleryImagesPermission()
                    )

                    ActivityCompat.requestPermissions(this, permissions, 113)
                }
                else openGallery()

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
        if (requestCode == 112) {
            val cameraGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED

            if (cameraGranted)
                openCamera()
        }
        // if gallery permissions have been granted, open the gallery

        else if (requestCode == 113) {
            val galleryImagesPermissionGranted =
                grantResults[0] == PackageManager.PERMISSION_GRANTED

            if (galleryImagesPermissionGranted)
                openGallery()
        }
    }
}
