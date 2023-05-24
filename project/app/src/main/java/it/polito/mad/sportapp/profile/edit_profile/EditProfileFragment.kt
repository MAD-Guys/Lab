package it.polito.mad.sportapp.profile.edit_profile

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.application_utilities.clearStorageFiles
import it.polito.mad.sportapp.application_utilities.fastblur
import it.polito.mad.sportapp.profile.Gender
import it.polito.mad.sportapp.profile.Level
import it.polito.mad.sportapp.profile.ProfileViewModel
import it.polito.mad.sportapp.profile.Sport
import it.polito.mad.sportapp.profile.SportChips
import it.polito.mad.sportapp.application_utilities.rotateBitmap
import it.polito.mad.sportapp.application_utilities.uriToBitmap

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    // user info fields' temporary state
    internal var firstNameTemp: String? = null
    internal var lastNameTemp: String? = null
    internal var usernameTemp: String? = null
    internal var ageTemp: String? = null
    internal var radioGenderCheckedTemp: Gender? = null
    internal var locationTemp: String? = null
    internal var bioTemp: String? = null

    // action bar
    internal var actionBar: ActionBar? = null

    // navigation controller
    internal lateinit var navController: NavController

    // edit profile view model
    internal val vm by activityViewModels<ProfileViewModel>()

    // Sports temporary state
    internal var sportsTemp: MutableMap<String, Sport> = mutableMapOf()

    // used to distinguish between tapped sports
    internal lateinit var consideredSport: String

    // User info views
    internal lateinit var firstName: EditText
    internal lateinit var lastName: EditText
    internal lateinit var username: EditText
    internal lateinit var age: EditText
    internal lateinit var genderRadioGroup: RadioGroup
    internal lateinit var location: EditText
    internal lateinit var bio: EditText

    // Sports views: each element contains the FireSport Chip and the actual level icon Chip
    internal val sports = HashMap<String, SportChips>()

    // Profile picture
    internal lateinit var profilePicture: ImageView
    internal lateinit var backgroundProfilePicture: ImageView
    internal var profilePictureBitmap: Bitmap? = null
    internal var backgroundProfilePictureBitmap: Bitmap? = null

    internal var cameraUri: Uri? = null
    private lateinit var cropImageOptions: CropImageOptions

    // gallery permission launcher
    private val galleryRequestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // gallery permission is granted
            openGallery()
        } else {
            // gallery permission is not granted
            Log.d("EditProfileFragment", "Gallery permission not granted!")
        }
    }

    /* results launchers */
    internal var galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
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

    // camera permission launcher
    private val cameraRequestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // camera permission is granted
            openCamera()
        } else {
            // camera permission is not granted
            Log.d("EditProfileFragment", "Camera permission not granted!")
        }
    }

    internal var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {

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
                uriToBitmap(uri, requireActivity().contentResolver)?.let { bitmap ->
                    rotateBitmap(uri, bitmap, requireActivity().contentResolver)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // initialize navigation controller
        navController = findNavController()

        // initialize menu
        menuInit()

        // hide bottom bar
        val bottomBar =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)
        bottomBar.visibility = View.GONE

        // setup back button callback
        setupOnBackPressedCallback()

        // initialize the EditText views
        firstName = view.findViewById(R.id.edit_first_name)
        lastName = view.findViewById(R.id.edit_last_name)
        username = view.findViewById(R.id.edit_username)
        genderRadioGroup = view.findViewById(R.id.radio_gender_group)
        age = view.findViewById(R.id.edit_age)
        location = view.findViewById(R.id.edit_location)
        bio = view.findViewById(R.id.edit_bio)
        profilePicture = view.findViewById(R.id.profile_picture)
        backgroundProfilePicture = view.findViewById(R.id.background_profile_picture)

        // setup views observers
        observersSetup()

        // initialize options to crop the profile picture
        cropImageOptions = CropImageOptions(
            guidelines = CropImageView.Guidelines.ON,
            outputCompressFormat = Bitmap.CompressFormat.JPEG,
            outputCompressQuality = 100,
            autoZoomEnabled = true,
            allowFlipping = true,
            allowRotation = true,
            fixAspectRatio = true,
            aspectRatioX = 1,
            aspectRatioY = 1
        )

        // load profile pictures from storage
        loadPicturesFromInternalStorage()

        /* manage listeners */

        // set context menu to change profile picture
        val profileImageButton: ImageButton = view.findViewById(R.id.profile_picture_button)
        registerForContextMenu(profileImageButton)
        // disable long default click listener
        profileImageButton.setOnLongClickListener(null)

        // set click listener to change profile picture
        profileImageButton.setOnClickListener {
            // open the related context menu
            requireActivity().openContextMenu(profileImageButton)
        }

        // add listeners to the user info views
        firstName.addTextChangedListener(textListenerInit("firstName"))
        lastName.addTextChangedListener(textListenerInit("lastName"))
        username.addTextChangedListener(textListenerInit("username"))
        age.addTextChangedListener(textListenerInit("age"))
        location.addTextChangedListener(textListenerInit("location"))
        bio.addTextChangedListener(textListenerInit("bio"))

        genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            radioGenderCheckedTemp = when (checkedId) {
                R.id.radio_male -> Gender.Male
                R.id.radio_female -> Gender.Female
                R.id.radio_other -> Gender.Other
                else -> throw RuntimeException("An unexpected Gender Button id has been detected")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // initialize temporary variables
        initializeTempVariables()
    }

    override fun onPause() {
        super.onPause()

        // save the user information on internal storage
        saveInformationOnStorage()
    }

    override fun onDestroy() {
        super.onDestroy()

        // the clearStorageFile function is called here because
        // the cache must be cleared before the EditProfileActivity ends but
        // cannot be cleared before the picture is cropped

        // delete the temporary profile pictures saved into cache (if any)
        clearStorageFiles(requireActivity().cacheDir, "temp_profile_picture[a-zA-Z0-9]*.jpeg")
    }

    /* context menu (to choose how to change profile picture) */

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater

        when (v.id) {
            R.id.profile_picture_button -> {
                inflater.inflate(R.menu.profile_picture_context_menu, menu)
            }

            R.id.actual_level_chip -> {
                inflater.inflate(R.menu.sport_level_context_menu, menu)
                // set menu title
                menu.setHeaderTitle("Choose your level \uD83D\uDD25")
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // detect which item has been selected by the user in the 'change profile picture' menu
        return when (item.itemId) {
            R.id.camera -> {
                // check if camera permissions have already been granted or not
                cameraRequestPermissionLauncher.launch(Manifest.permission.CAMERA)
                true
            }

            R.id.gallery -> {
                // check if gallery permissions have already been granted or not
                galleryRequestPermissionLauncher.launch(galleryImagesPermission())
                true
            }
            // sport level menu options
            R.id.beginner_sport_level -> {
                // retrieve tapped sport
                val tappedSport = consideredSport
                // change level
                changeSportLevel(tappedSport, Level.BEGINNER)
                true
            }

            R.id.intermediate_sport_level -> {
                // retrieve tapped sport
                val tappedSport = consideredSport
                // change level
                changeSportLevel(tappedSport, Level.INTERMEDIATE)
                true
            }

            R.id.expert_sport_level -> {
                // retrieve tapped sport
                val tappedSport = consideredSport
                // change level
                changeSportLevel(tappedSport, Level.EXPERT)
                true
            }

            R.id.pro_sport_level -> {
                // retrieve tapped sport
                val tappedSport = consideredSport
                // change level
                changeSportLevel(tappedSport, Level.PRO)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

}