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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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
    // Sports temporary state
    private var sportSelectedTemp: MutableList<Boolean> = mutableListOf(
        false, false, false, false, false, false, false, false, false, false
    ) // 10 values initially set to false
    private var sportLevelTemp: MutableList<Level> = mutableListOf(
        Level.BEGINNER, Level.BEGINNER, Level.BEGINNER, Level.BEGINNER, Level.BEGINNER,
        Level.BEGINNER, Level.BEGINNER, Level.BEGINNER, Level.BEGINNER, Level.BEGINNER
    ) // 10 values initially set to BEGINNER


    // User info views
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var username: EditText
    private lateinit var age: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var location: EditText
    private lateinit var bio: EditText

    // Sports
    private lateinit var sportChips: MutableList<Chip>
    private lateinit var sportLevels: MutableList<ChipGroup>
    private lateinit var sportLevelChips: MutableList<MutableList<Chip>>

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

        // initialize sports and levels
        sportChips = mutableListOf()
        sportLevels = mutableListOf()
        sportLevelChips = mutableListOf()
        sportsInit() //fills the sportChips and sportLevels lists

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

        for( (index, sport) in sportChips.withIndex() ){
            sport.setOnClickListener { sportChipListener(index) }
        }
        for( (index, sportLevel) in sportLevels.withIndex() ){
            sportLevel.setOnCheckedStateChangeListener { it, _ -> sportLevelListener(it, index) }
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

        // retrieve sports
        val basketJSON :JSONObject? = jsonObjectProfile?.optJSONObject("basket") ?: null
        var basketResume :Sport? = null
        if(basketJSON != null) basketResume = Sport(basketJSON!!.getBoolean("selected"), basketJSON!!.getInt("level"))

        val football11JSON :JSONObject? = jsonObjectProfile?.optJSONObject("football11") ?: null
        var football11Resume :Sport? = null
        if(football11JSON != null) football11Resume = Sport(football11JSON!!.getBoolean("selected"), football11JSON!!.getInt("level"))

        val football5JSON :JSONObject? = jsonObjectProfile?.optJSONObject("football5") ?: null
        var football5Resume :Sport? = null
        if(football5JSON != null) football5Resume = Sport(football5JSON!!.getBoolean("selected"), football5JSON!!.getInt("level"))

        val football8JSON :JSONObject? = jsonObjectProfile?.optJSONObject("football8") ?: null
        var football8Resume :Sport? = null
        if(football8JSON != null) football8Resume = Sport(football8JSON!!.getBoolean("selected"), football8JSON!!.getInt("level"))

        val tennisJSON :JSONObject? = jsonObjectProfile?.optJSONObject("tennis") ?: null
        var tennisResume :Sport? = null
        if(tennisJSON != null) football5Resume = Sport(tennisJSON!!.getBoolean("selected"), tennisJSON!!.getInt("level"))

        val volleyballJSON :JSONObject? = jsonObjectProfile?.optJSONObject("volleyball") ?: null
        var volleyballResume :Sport? = null
        if(volleyballJSON != null) volleyballResume = Sport(volleyballJSON!!.getBoolean("selected"), volleyballJSON!!.getInt("level"))

        val tableTennisJSON :JSONObject? = jsonObjectProfile?.optJSONObject("tableTennis") ?: null
        var tableTennisResume :Sport? = null
        if(tableTennisJSON != null) tableTennisResume = Sport(tableTennisJSON!!.getBoolean("selected"), tableTennisJSON!!.getInt("level"))

        val beachVolleyJSON :JSONObject? = jsonObjectProfile?.optJSONObject("beachVolley") ?: null
        var beachVolleyResume :Sport? = null
        if(beachVolleyJSON != null) beachVolleyResume = Sport(beachVolleyJSON!!.getBoolean("selected"), beachVolleyJSON!!.getInt("level"))

        val padelJSON :JSONObject? = jsonObjectProfile?.optJSONObject("padel") ?: null
        var padelResume :Sport? = null
        if(padelJSON != null) padelResume = Sport(padelJSON!!.getBoolean("selected"), padelJSON!!.getInt("level"))

        val miniGolfJSON :JSONObject? = jsonObjectProfile?.optJSONObject("miniGolf") ?: null
        var miniGolfResume :Sport? = null
        if(miniGolfJSON != null) miniGolfResume = Sport(miniGolfJSON!!.getBoolean("selected"), miniGolfJSON!!.getInt("level"))


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

        // set sports Edit fields and temporary values
        if(basketResume != null && basketResume.selected){
            sportSelectedTemp[0] = true
            sportLevelTemp[0] = when(basketResume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[0].check( sportLevelChips[0][sportLevelTemp[0].ordinal].id )
            sportLevels[0].visibility = ChipGroup.VISIBLE
        }
        if(football11Resume != null && football11Resume.selected){
            sportSelectedTemp[1] = true
            sportLevelTemp[1] = when(football11Resume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[1].check( sportLevelChips[1][sportLevelTemp[1].ordinal].id )
            sportLevels[1].visibility = ChipGroup.VISIBLE
        }
        if(football5Resume != null && football5Resume.selected){
            sportSelectedTemp[2] = true
            sportLevelTemp[2] = when(football5Resume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[2].check( sportLevelChips[2][sportLevelTemp[2].ordinal].id )
            sportLevels[2].visibility = ChipGroup.VISIBLE
        }
        if(football8Resume != null && football8Resume.selected){
            sportSelectedTemp[3] = true
            sportLevelTemp[3] = when(football8Resume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[3].check( sportLevelChips[3][sportLevelTemp[3].ordinal].id )
            sportLevels[3].visibility = ChipGroup.VISIBLE
        }
        if(tennisResume != null && tennisResume.selected){
            sportSelectedTemp[4] = true
            sportLevelTemp[4] = when(tennisResume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[4].check( sportLevelChips[4][sportLevelTemp[4].ordinal].id )
            sportLevels[4].visibility = ChipGroup.VISIBLE
        }
        if(volleyballResume != null && volleyballResume.selected){
            sportSelectedTemp[5] = true
            sportLevelTemp[5] = when(volleyballResume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[5].check( sportLevelChips[5][sportLevelTemp[5].ordinal].id )
            sportLevels[5].visibility = ChipGroup.VISIBLE
        }
        if(tableTennisResume != null && tableTennisResume.selected){
            sportSelectedTemp[6] = true
            sportLevelTemp[6] = when(tableTennisResume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[6].check( sportLevelChips[6][sportLevelTemp[6].ordinal].id )
            sportLevels[6].visibility = ChipGroup.VISIBLE
        }
        if(beachVolleyResume != null && beachVolleyResume.selected){
            sportSelectedTemp[7] = true
            sportLevelTemp[7] = when(beachVolleyResume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[7].check( sportLevelChips[7][sportLevelTemp[7].ordinal].id )
            sportLevels[7].visibility = ChipGroup.VISIBLE
        }
        if(padelResume != null && padelResume.selected){
            sportSelectedTemp[8] = true
            sportLevelTemp[8] = when(padelResume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[8].check( sportLevelChips[8][sportLevelTemp[8].ordinal].id )
            sportLevels[8].visibility = ChipGroup.VISIBLE
        }
        if(miniGolfResume != null && miniGolfResume.selected){
            sportSelectedTemp[9] = true
            sportLevelTemp[9] = when(miniGolfResume.level){
                0 -> Level.BEGINNER
                1 -> Level.INTERMEDIATE
                2 -> Level.EXPERT
                3 -> Level.PRO
                else -> Level.BEGINNER //never used
            }
            sportLevels[9].check( sportLevelChips[9][sportLevelTemp[9].ordinal].id )
            sportLevels[9].visibility = ChipGroup.VISIBLE
        }

    }

    private fun saveInformationOnStorage() {
        // the temporary information is *serialized* firstly into a JSONObject
        // and then into the sharedPreferences file with the key *profile*
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

        // save sports
        val basket = Sport(sportSelectedTemp[0], sportLevelTemp[0].ordinal)
        jsonObjectProfile.put("basket", basket)
        val football11 = Sport(sportSelectedTemp[1], sportLevelTemp[1].ordinal)
        jsonObjectProfile.put("football11", football11)
        val football5 = Sport(sportSelectedTemp[2], sportLevelTemp[2].ordinal)
        jsonObjectProfile.put("football5", football5)
        val football8 = Sport(sportSelectedTemp[3], sportLevelTemp[3].ordinal)
        jsonObjectProfile.put("football8", football8)
        val tennis = Sport(sportSelectedTemp[4], sportLevelTemp[4].ordinal)
        jsonObjectProfile.put("tennis", tennis)
        val volleyball = Sport(sportSelectedTemp[5], sportLevelTemp[5].ordinal)
        jsonObjectProfile.put("volleyball", volleyball)
        val tableTennis = Sport(sportSelectedTemp[6], sportLevelTemp[6].ordinal)
        jsonObjectProfile.put("tableTennis", tableTennis)
        val beachVolley = Sport(sportSelectedTemp[7], sportLevelTemp[7].ordinal)
        jsonObjectProfile.put("beachVolley", beachVolley)
        val padel = Sport(sportSelectedTemp[8], sportLevelTemp[8].ordinal)
        jsonObjectProfile.put("padel", padel)
        val miniGolf = Sport(sportSelectedTemp[9], sportLevelTemp[9].ordinal)
        jsonObjectProfile.put("miniGolf", miniGolf)

        // apply changes and show a pop up to the user
        editor.putString("profile", jsonObjectProfile.toString())
        editor.apply()
    }

    override fun onPause() {
        super.onPause()

        // save the temporary profile variables into the sharedPreferences file
        saveInformationOnStorage()

        // delete the temporary profile pictures saved into cache (if any)
        clearStorageFiles(cacheDir, "temp_profile_picture[a-zA-Z0-9]*.jpeg")
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
            // (the information will be persistently saved in the onPause method)

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

    /*----- SPORTS UTILITIES -----*/
    private data class Sport(val selected :Boolean, val level :Int)
    private enum class Level {
        BEGINNER, INTERMEDIATE, EXPERT, PRO
    }

    // Fills sport chips and sport level lists, the chips in xml are named with the sport name
    private fun sportsInit(){

        // SportChips
        val basket :Chip = findViewById(R.id.basketChip)
        val football11 :Chip = findViewById(R.id.football11Chip)
        val football5 :Chip = findViewById(R.id.football5Chip)
        val football8 :Chip = findViewById(R.id.football8Chip)
        val tennis :Chip = findViewById(R.id.tennisChip)
        val volleyball :Chip = findViewById(R.id.volleyballChip)
        val tableTennis :Chip = findViewById(R.id.tableTennisChip)
        val beachVolley :Chip = findViewById(R.id.beachVolleyChip)
        val padel :Chip = findViewById(R.id.padelChip)
        val miniGolf :Chip = findViewById(R.id.miniGolfChip)

        // SportLevels
        val basketLevel :ChipGroup = findViewById(R.id.basketLevelSelector)
        val football11Level :ChipGroup = findViewById(R.id.football11LevelSelector)
        val football5Level :ChipGroup = findViewById(R.id.football5LevelSelector)
        val football8Level :ChipGroup = findViewById(R.id.football8LevelSelector)
        val tennisLevel :ChipGroup = findViewById(R.id.tennisLevelSelector)
        val volleyballLevel :ChipGroup = findViewById(R.id.volleyballLevelSelector)
        val tableTennisLevel :ChipGroup = findViewById(R.id.tableTennisLevelSelector)
        val beachVolleyLevel :ChipGroup = findViewById(R.id.beachVolleyLevelSelector)
        val padelLevel :ChipGroup = findViewById(R.id.padelLevelSelector)
        val miniGolfLevel :ChipGroup = findViewById(R.id.miniGolfLevelSelector)

        // Add items to lists
        // To make changes easier, insertions are ordered by sport

        sportChips.add(0, basket)
        sportLevels.add(0, basketLevel)

        sportChips.add(1, football11)
        sportLevels.add(1, football11Level)

        sportChips.add(2, football5)
        sportLevels.add(2, football5Level)

        sportChips.add(3, football8)
        sportLevels.add(3, football8Level)

        sportChips.add(4, tennis)
        sportLevels.add(4, tennisLevel)

        sportChips.add(5, volleyball)
        sportLevels.add(5, volleyballLevel)

        sportChips.add(6, tableTennis)
        sportLevels.add(6, tableTennisLevel)

        sportChips.add(7, beachVolley)
        sportLevels.add(7, beachVolleyLevel)

        sportChips.add(8, padel)
        sportLevels.add(8, padelLevel)

        sportChips.add(9, miniGolf)
        sportLevels.add(9, miniGolfLevel)

        // level chips
        sportLevelChips.add(0, mutableListOf())
        sportLevelChips[0].add(0, findViewById(R.id.basketBeginner))
        sportLevelChips[0].add(1, findViewById(R.id.basketIntermediate))
        sportLevelChips[0].add(2, findViewById(R.id.basketExpert))
        sportLevelChips[0].add(3, findViewById(R.id.basketPro))

        sportLevelChips.add(1, mutableListOf())
        sportLevelChips[1].add(0, findViewById(R.id.football11Beginner))
        sportLevelChips[1].add(1, findViewById(R.id.football11Intermediate))
        sportLevelChips[1].add(2, findViewById(R.id.football11Expert))
        sportLevelChips[1].add(3, findViewById(R.id.football11Pro))

        sportLevelChips.add(2, mutableListOf())
        sportLevelChips[2].add(0, findViewById(R.id.football5Beginner))
        sportLevelChips[2].add(1, findViewById(R.id.football5Intermediate))
        sportLevelChips[2].add(2, findViewById(R.id.football5Expert))
        sportLevelChips[2].add(3, findViewById(R.id.football5Pro))

        sportLevelChips.add(3, mutableListOf())
        sportLevelChips[3].add(0, findViewById(R.id.football8Beginner))
        sportLevelChips[3].add(1, findViewById(R.id.football8Intermediate))
        sportLevelChips[3].add(2, findViewById(R.id.football8Expert))
        sportLevelChips[3].add(3, findViewById(R.id.football8Pro))

        sportLevelChips.add(4, mutableListOf())
        sportLevelChips[4].add(0, findViewById(R.id.tennisBeginner))
        sportLevelChips[4].add(1, findViewById(R.id.tennisIntermediate))
        sportLevelChips[4].add(2, findViewById(R.id.tennisExpert))
        sportLevelChips[4].add(3, findViewById(R.id.tennisPro))

        sportLevelChips.add(5, mutableListOf())
        sportLevelChips[5].add(0, findViewById(R.id.volleyballBeginner))
        sportLevelChips[5].add(1, findViewById(R.id.volleyballIntermediate))
        sportLevelChips[5].add(2, findViewById(R.id.volleyballExpert))
        sportLevelChips[5].add(3, findViewById(R.id.volleyballPro))

        sportLevelChips.add(6, mutableListOf())
        sportLevelChips[6].add(0, findViewById(R.id.tableTennisBeginner))
        sportLevelChips[6].add(1, findViewById(R.id.tableTennisIntermediate))
        sportLevelChips[6].add(2, findViewById(R.id.tableTennisExpert))
        sportLevelChips[6].add(3, findViewById(R.id.tableTennisPro))

        sportLevelChips.add(7, mutableListOf())
        sportLevelChips[7].add(0, findViewById(R.id.beachVolleyBeginner))
        sportLevelChips[7].add(1, findViewById(R.id.beachVolleyIntermediate))
        sportLevelChips[7].add(2, findViewById(R.id.beachVolleyExpert))
        sportLevelChips[7].add(3, findViewById(R.id.beachVolleyPro))

        sportLevelChips.add(8, mutableListOf())
        sportLevelChips[8].add(0, findViewById(R.id.padelBeginner))
        sportLevelChips[8].add(1, findViewById(R.id.padelIntermediate))
        sportLevelChips[8].add(2, findViewById(R.id.padelExpert))
        sportLevelChips[8].add(3, findViewById(R.id.padelPro))

        sportLevelChips.add(9, mutableListOf())
        sportLevelChips[9].add(0, findViewById(R.id.miniGolfBeginner))
        sportLevelChips[9].add(1, findViewById(R.id.miniGolfIntermediate))
        sportLevelChips[9].add(2, findViewById(R.id.miniGolfExpert))
        sportLevelChips[9].add(3, findViewById(R.id.miniGolfPro))
    }

    //Generic listener to select or deselect a sport
    private fun sportChipListener(index :Int) {
        if(sportSelectedTemp[index]){ // this sport was already selected -> deselect it
            sportSelectedTemp[index] = false
            sportLevels[index].visibility = ChipGroup.GONE

        } else { // this sport was not selected -> select it
            sportSelectedTemp[index] = true
            sportLevels[index].check( sportLevelChips[index][sportLevelTemp[index].ordinal].id )
            sportLevels[index].visibility = ChipGroup.VISIBLE
        }
    }

    private fun sportLevelListener(chipGroup: ChipGroup, index: Int) {
        when (chipGroup.checkedChipId) {
            sportLevelChips[index][Level.BEGINNER.ordinal].id -> sportLevelTemp[index] = Level.BEGINNER
            sportLevelChips[index][Level.INTERMEDIATE.ordinal].id -> sportLevelTemp[index] = Level.INTERMEDIATE
            sportLevelChips[index][Level.EXPERT.ordinal].id -> sportLevelTemp[index] = Level.EXPERT
            sportLevelChips[index][Level.PRO.ordinal].id -> sportLevelTemp[index] = Level.PRO
        }
        println("Set level ${sportLevelTemp[index]} for sport $index")
    }
}
