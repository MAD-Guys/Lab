package it.polito.mad.lab2

import android.content.Intent
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONObject
import java.io.File


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

    /*
    Sport("basket", sportSelectedTemp[0], sportLevelTemp[0]).saveAsJson(jsonObjectProfile)
    Sport("soccer11", sportSelectedTemp[1], sportLevelTemp[1]).saveAsJson(jsonObjectProfile)
    Sport("soccer5", sportSelectedTemp[2], sportLevelTemp[2]).saveAsJson(jsonObjectProfile)
    Sport("soccer8", sportSelectedTemp[3], sportLevelTemp[3]).saveAsJson(jsonObjectProfile)
    Sport("tennis", sportSelectedTemp[4], sportLevelTemp[4]).saveAsJson(jsonObjectProfile)
    Sport("volleyball", sportSelectedTemp[5], sportLevelTemp[5]).saveAsJson(jsonObjectProfile)
    Sport("tableTennis", sportSelectedTemp[6], sportLevelTemp[6]).saveAsJson(jsonObjectProfile)
    Sport("beachVolley", sportSelectedTemp[7], sportLevelTemp[7]).saveAsJson(jsonObjectProfile)
    Sport("padel", sportSelectedTemp[8], sportLevelTemp[8]).saveAsJson(jsonObjectProfile)
    Sport("miniGolf", sportSelectedTemp[9], sportLevelTemp[9]).saveAsJson(jsonObjectProfile)
    */

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

/*----- SPORTS UTILITIES -----*/

// Fills sport chips and sport level lists, the chips in xml are named with the sport name
internal fun EditProfileActivity.sportsInit() {
    val sportsContainer = findViewById<ChipGroup>(R.id.sports_container)

    for (sport in sportsTemp.keys) {
        // create the vertical wrapper
        val linearLayout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // width
                LinearLayout.LayoutParams.WRAP_CONTENT  // height
            )

            orientation = LinearLayout.VERTICAL
        }

        // create the Sport Chip
        val sportChip = Chip(this).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,                    // width
                resources.getDimension(R.dimen.chip_height).toInt()     // height
            ).apply {
                marginEnd = resources.getDimension(R.dimen.chip_margin_end).toInt()
            }
            isCheckable = true
            typeface = ResourcesCompat.getFont(context, R.font.poppins_medium)
            text = extendedNameOf(sport)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.chip_text_size))
            setTextColor(getColor(R.color.unselected_chip_text_color))
            visibility = VISIBLE
            checkedIcon?.setVisible(true, true)
            setChipBackgroundColorResource(R.color.unselected_chip_color)
            setChipStrokeColorResource(R.color.unselected_chip_border_color)
            chipStrokeWidth = 1.0f.dpToPx(context).toFloat()
            setTextStartPaddingResource(R.dimen.edit_chip_text_start_padding)
            setTextEndPaddingResource(R.dimen.edit_chip_text_end_padding)
        }

        // create the levels wrapper
        val sportLevelsChipGroup = ChipGroup(this).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,                    // width
                resources.getDimension(R.dimen.chip_icon_size).toInt()  // height
            )
            setPadding(0, 0, 0, 8.0f.dpToPx(context))     // padding bottom
            visibility = GONE
            isSingleSelection = true
            isSelectionRequired = true
        }

        // create levels badges
        val sportLevelsChips = listOf(
            R.drawable.beginner_level_badge,
            R.drawable.intermediate_level_badge,
            R.drawable.expert_level_badge,
            R.drawable.pro_level_badge
        ).map {level_badge ->
            Chip(this).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,    // width
                    ViewGroup.LayoutParams.MATCH_PARENT     // height
                )
                isCheckable = true
                setChipBackgroundColorResource(R.color.background_orange)
                chipEndPadding = 4.0f
                setChipIconResource(level_badge)
                setChipIconSizeResource(R.dimen.chip_icon_size)
                // (no surface color)
                setRippleColorResource(R.color.background_orange)
                textEndPadding = 0f
                textStartPadding = 0f
            }
        }

        // build sport hierarchy
        sportLevelsChips.forEach { chip -> sportLevelsChipGroup.addView(chip) }
        linearLayout.addView(sportChip)
        linearLayout.addView(sportLevelsChipGroup)
        // append everything to the sport container
        sportsContainer.addView(linearLayout)

        // save views
        sports[sport] = SportChips(sport, sportChip, sportLevelsChipGroup, sportLevelsChips)
    }
}

// Generic listener to select or deselect a sport
internal fun EditProfileActivity.sportChipListener(sportName: String) {
    val sport = sports[sportName]!!
    val sportTemp = sportsTemp[sportName]!!

    if (sportTemp.selected) { // this sport was already selected -> deselect it
        sportTemp.selected = false
        sport.levelsChipGroup.visibility = ChipGroup.GONE
    }
    else { // this sport was not selected -> select it
        sportTemp.selected = true
        sport.levelsChips[sportTemp.level.ordinal].isChecked = true
        sport.levelsChipGroup.visibility = ChipGroup.VISIBLE
    }
}

internal fun EditProfileActivity.sportLevelListener(chipGroup: ChipGroup, sportName: String) {
    val sport = sports[sportName]!!
    val sportTemp = sportsTemp[sportName]!!

    sportTemp.level = when (chipGroup.checkedChipId) {
        sport.levelsChips[Level.BEGINNER.ordinal].id -> Level.BEGINNER
        sport.levelsChips[Level.INTERMEDIATE.ordinal].id -> Level.INTERMEDIATE
        sport.levelsChips[Level.EXPERT.ordinal].id -> Level.EXPERT
        sport.levelsChips[Level.PRO.ordinal].id -> Level.PRO
        else -> throw RuntimeException("Detected unexpected checked chip id")
    }

    println("Set level ${sportTemp.level} for sport $sportName")
}

// The function set sports Edit fields and temporary values
private fun EditProfileActivity.setEditSportsField(sport: Sport) {
    if (sport.selected) {
        // manage sport state
        sportsTemp[sport.name] = sport

        // manage sport views
        val sportGroup = sports[sport.name]!!
        sportGroup.chip.isChecked = true
        sportGroup.levelsChips[sport.level.ordinal].isChecked = true
        sportGroup.levelsChipGroup.visibility = ChipGroup.VISIBLE
    }
}

private fun EditProfileActivity.setHardcodedSportFields(vararg hardcodedSports: Sport) {
    hardcodedSports.forEach {
        // save state
        sportsTemp[it.name] = it

        // manage views
        val sportGroup = sports[it.name]!!
        sportGroup.chip.isChecked = true
        sportGroup.levelsChips[it.level.ordinal].isChecked = true
        sportGroup.levelsChipGroup.visibility = ChipGroup.VISIBLE
    }
}
