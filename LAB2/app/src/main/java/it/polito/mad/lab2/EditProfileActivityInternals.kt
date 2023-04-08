package it.polito.mad.lab2

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONObject
import java.io.File


/* manage load/save from/into storage */

internal fun EditProfileActivity.loadDataFromStorage() {
    // retrieve data from SharedPreferences
    val sh = getSharedPreferences("it.polito.mad.lab2", AppCompatActivity.MODE_PRIVATE)
    val jsonObjectProfile: JSONObject? = sh.getString("profile", null)?.let { JSONObject(it) }

    /* manage user info */

    // retrieve data from the JSON object
    val firstNameResume = jsonObjectProfile?.getString("firstName") ?: getString(R.string.first_name)
    val lastNameResume = jsonObjectProfile?.getString("lastName") ?: getString(R.string.last_name)
    val usernameResume = jsonObjectProfile?.getString("username") ?: getString(R.string.username)
    val genderResume = Gender.valueOf(jsonObjectProfile?.getString("gender") ?: getString(R.string.male_gender))
    val ageResume = jsonObjectProfile?.getString("age") ?: getString(R.string.user_age)
    val locationResume = jsonObjectProfile?.getString("location") ?: getString(R.string.user_location)
    val bioResume = jsonObjectProfile?.getString("bio") ?: getString(R.string.user_bio)

    // set EditText views
    firstName.setText(firstNameResume)
    lastName.setText(lastNameResume)
    username.setText(usernameResume)
    age.setText(ageResume)
    location.setText(locationResume)
    bio.setText(bioResume)

    when (genderResume) {
        Gender.Male -> genderRadioGroup.check(R.id.radio_male)
        Gender.Female -> genderRadioGroup.check(R.id.radio_female)
        Gender.Other -> genderRadioGroup.check(R.id.radio_other)
    }

    // set temporary variables
    firstNameTemp = firstNameResume
    lastNameTemp = lastNameResume
    usernameTemp = usernameResume
    radioGenderCheckedTemp = genderResume
    ageTemp = ageResume
    locationTemp = locationResume
    bioTemp = bioResume

    /* manage profile and background picture */

    // retrieve profile picture and update it with the one uploaded by the user, if any
    getPictureFromInternalStorage(filesDir, "profilePicture.jpeg")?.let {
        profilePicture.setImageBitmap(it)
    }

    // retrieve background picture and update it with the one uploaded by the user, if any
    getPictureFromInternalStorage(filesDir, "backgroundProfilePicture.jpeg")?.let {
        backgroundProfilePicture.setImageBitmap(it)
    }

    /* manage user sports */

    // set hard coded sports the first time the app is launched
    if (jsonObjectProfile == null)
        setHardcodedSportFields(*getHardcodedSports())
    else {
        // retrieve sports from storage
        val sportJson = jsonObjectProfile.getJSONObject("sports")
        sportsTemp.keys.forEach { sportName ->
            val sportResume = Sport.from(sportName, sportJson)
            setEditSportsField(sportResume)
        }
    }
}

internal fun EditProfileActivity.saveInformationOnStorage() {
    // the temporary information is *serialized* firstly into a JSONObject
    // and then into the sharedPreferences file with the key *profile*
    val sh = getSharedPreferences("it.polito.mad.lab2", AppCompatActivity.MODE_PRIVATE)
    val editor = sh.edit()

    val jsonObjectProfile = JSONObject()

    // serializing the temporary profile variables into the JSONObject
    jsonObjectProfile.put("firstName", firstNameTemp)
    jsonObjectProfile.put("lastName", lastNameTemp)
    jsonObjectProfile.put("username", usernameTemp)
    jsonObjectProfile.put("age", ageTemp)
    jsonObjectProfile.put("location", locationTemp)
    jsonObjectProfile.put("bio", bioTemp)
    jsonObjectProfile.put("gender", radioGenderCheckedTemp?.name)

    // save profile and background pictures into the internal storage
    profilePictureBitmap?.let {
        savePictureOnInternalStorage(it, filesDir, "profilePicture.jpeg")
    }
    backgroundProfilePictureBitmap?.let {
        savePictureOnInternalStorage(it, filesDir, "backgroundProfilePicture.jpeg")
    }

    // save sports as a JsonObject
    val sportJson = JSONObject()
    sportsTemp.forEach{ (_, sport) -> sport.saveAsJson(sportJson) }
    jsonObjectProfile.put("sports", sportJson)

    // apply changes
    editor.putString("profile", jsonObjectProfile.toString())
    editor.apply()
}

internal fun EditProfileActivity.textListenerInit(fieldName: String): TextWatcher {
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

/* start new Activities to get a new picture */

internal fun EditProfileActivity.openCamera() {
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

internal fun EditProfileActivity.openGallery() {
    // create intent and open phone gallery
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    galleryActivityResultLauncher.launch(galleryIntent)
}

internal fun galleryImagesPermission(): String =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE



/*----- SPORTS UTILITIES -----*/

// Fills sport chips and sport level lists, the chips in xml are named with the sport name
internal fun EditProfileActivity.sportsInit() {
    val sportsContainer = findViewById<ChipGroup>(R.id.sports_container)

    for (sport in sportsTemp.keys) {
        // create the vertical wrapper
        val linearLayout = LinearLayout(this).apply {
            layoutParams = LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // width
                LinearLayout.LayoutParams.WRAP_CONTENT  // height
            )
            orientation = LinearLayout.VERTICAL
        }

        // create the horizontal sport chip wrapper
        // (it will contain the sport chip and the level badge, if any)
        val sportChipWrapper = LinearLayout(this).apply {
            layoutParams = LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // width
                LinearLayout.LayoutParams.WRAP_CONTENT  // height
            )
            orientation = LinearLayout.HORIZONTAL
        }

        // create the Sport Chip
        val sportChip = createEditSportChip(sport, linearLayout)

        // create the actual Sport level chip
        val sportActualLevelChip = createEditSportLevelBadge(R.drawable.beginner_level_badge, sportChipWrapper)

        // build sport hierarchy
        sportChipWrapper.addView(sportChip)
        sportChipWrapper.addView(sportActualLevelChip)
        linearLayout.addView(sportChipWrapper)

        // append everything to the sport container
        sportsContainer.addView(linearLayout)

        // save views
        sports[sport] = SportChips(sport, sportChip, sportActualLevelChip)
    }
}

private fun EditProfileActivity.createEditSportChip(sportName: String, parent: ViewGroup): Chip {
    // inflate generic sport chip
    val sportChip = layoutInflater.inflate(R.layout.edit_profile_chip, parent, false) as Chip
    // customize it for the provided sport
    sportChip.text = extendedNameOf(sportName)

    return sportChip
}

private fun EditProfileActivity.createEditSportLevelBadge(levelIconResource: Int, parent: ViewGroup): Chip {
    // infate generic level badge
    val levelChip = layoutInflater.inflate(
        if(levelIconResource == R.drawable.intermediate_level_badge)
            R.layout.edit_profile_sport_level_chip_big
        else
            R.layout.edit_profile_sport_level_chip,
        parent,
        false) as Chip

    // customize icon based on the level
    levelChip.setChipIconResource(levelIconResource)

    // (this is the level badge next to the sport chip)
    levelChip.isCheckable = false
    levelChip.visibility = Chip.GONE
    levelChip.layoutParams = LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT
    )

    return levelChip
}

// Generic listener to select or deselect a sport badge
internal fun EditProfileActivity.sportChipListener(sportName: String) {
    val sport = sports[sportName]!!
    val sportTemp = sportsTemp[sportName]!!

    if (sportTemp.selected) { // this sport was already selected -> deselect it
        sportTemp.selected = false
        sportTemp.level = Level.NO_LEVEL    // reset level

        // uncheck sport icon
        sport.chip.isChecked = false
        // hide actual level image
        sport.actualLevelChip.visibility = Chip.GONE
    }
    else {
        // update sport considered at the moment by the menu
        consideredSport = sportName

        // this sport was not selected -> open the context menu to choose the level
        openContextMenu(sport.actualLevelChip)

        // hide the checked icon on the sport badge
        sport.chip.isChecked = false
    }
}

internal fun EditProfileActivity.changeSportLevel(sportName: String, level: Level) {
    val sport = sportsTemp[sportName]!!
    val sportChips = sports[sportName]!!

    // mark sport as selected and set new level
    sport.selected = true
    sport.level = level

    // mark sport chip as selected and show proper level badge
    sportChips.chip.isChecked = true
    sportChips.actualLevelChip.setChipIconResource(level.icon())
    sportChips.actualLevelChip.setChipIconSizeResource(
        if (level == Level.INTERMEDIATE)
            R.dimen.chip_icon_size_big
        else
            R.dimen.chip_icon_size
    )

    sportChips.actualLevelChip.visibility = Chip.VISIBLE
}

// Set sports Edit fields and temporary values
private fun EditProfileActivity.setEditSportsField(sport: Sport) {
    if (sport.selected) {
        // manage sport state
        sportsTemp[sport.name] = sport

        // manage sport views
        val sportGroup = sports[sport.name]!!

        // check sport badge
        sportGroup.chip.isChecked = true

        // show the right level badge next to it
        sportGroup.actualLevelChip.setChipIconResource(sport.level.icon())
        sportGroup.actualLevelChip.setChipIconSizeResource(
            if (sport.level == Level.INTERMEDIATE)
                R.dimen.chip_icon_size_big
            else
                R.dimen.chip_icon_size
        )
        sportGroup.actualLevelChip.visibility = Chip.VISIBLE
    }
}

private fun EditProfileActivity.setHardcodedSportFields(vararg hardcodedSports: Sport) {
    hardcodedSports.forEach {
        setEditSportsField(it)
    }
}
