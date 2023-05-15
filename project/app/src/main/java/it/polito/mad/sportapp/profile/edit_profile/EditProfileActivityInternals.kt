package it.polito.mad.sportapp.profile.edit_profile

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.getPictureFromInternalStorage
import it.polito.mad.sportapp.profile.Gender
import it.polito.mad.sportapp.profile.Level
import it.polito.mad.sportapp.profile.Sport
import it.polito.mad.sportapp.profile.SportChips
import it.polito.mad.sportapp.entities.Sport as SportEntity
import it.polito.mad.sportapp.savePictureOnInternalStorage
import it.polito.mad.sportapp.setProfilePictureSize
import it.polito.mad.sportapp.showToasty
import java.io.File

// manage menu item selection
internal fun EditProfileFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.edit_profile_menu, menu)

            var menuHeight: Int = -1

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(false)
                it.title = "Edit Profile"
                menuHeight = it.height
            }

            val profilePictureContainer =
                requireView().findViewById<ConstraintLayout>(R.id.profile_picture_container)
            val backgroundProfilePicture =
                requireView().findViewById<ImageView>(R.id.background_profile_picture)
            val profilePicture = requireView().findViewById<ImageView>(R.id.profile_picture)

            // set profile picture height 1/3 of the app view
            requireActivity().setProfilePictureSize(
                menuHeight,
                profilePictureContainer,
                backgroundProfilePicture,
                profilePicture
            )
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // handle the menu selection
            return when (menuItem.itemId) {
                R.id.confirm_button -> {
                    // (the information will be persistently saved in the onPause method)

                    if (username.error == null) {
                        // navigate back to show profile fragment
                        navController.popBackStack()
                    } else {
                        showToasty("error", requireContext(), "Please fix the errors!")
                    }
                    true
                }

                else -> false
            }
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

internal fun EditProfileFragment.observersSetup() {

    // sports list observer
    vm.sportsList.observe(viewLifecycleOwner) { sportsList ->
        if (sportsList.isNotEmpty()) {
            setupTemporarySports(sportsList)
        }
    }

    // user first name observer
    vm.userFirstName.observe(viewLifecycleOwner) {
        firstName.setText(it)
    }

    // user last name observer
    vm.userLastName.observe(viewLifecycleOwner) {
        lastName.setText(it)
    }

    // user username observers
    vm.userUsername.observe(viewLifecycleOwner) {
        username.setText(it)
    }

    vm.usernameAlreadyExists.observe(viewLifecycleOwner) {
        if (it && username.text.toString() != vm.userUsername.value!!) {
            username.error = getString(R.string.username_already_exists_error)
        }
    }

    // user gender observer
    vm.userGender.observe(viewLifecycleOwner) {
        when (Gender.valueOf(it)) {
            Gender.Male -> genderRadioGroup.check(R.id.radio_male)
            Gender.Female -> genderRadioGroup.check(R.id.radio_female)
            Gender.Other -> genderRadioGroup.check(R.id.radio_other)
        }
    }

    // user age observer
    vm.userAge.observe(viewLifecycleOwner) {
        age.setText(it)
    }

    // user location observer
    vm.userLocation.observe(viewLifecycleOwner) {
        location.setText(it)
    }

    // user bio observer
    vm.userBio.observe(viewLifecycleOwner) {
        bio.setText(it)
    }
}

internal fun EditProfileFragment.setupTemporarySports(sportsList: List<SportEntity>) {
    val userSports = vm.userSports.value

    sportsList.forEach { tempSport ->

        if (!userSports.isNullOrEmpty()) {

            val userSport = userSports.find {
                it.sport == tempSport.name
            }

            if (userSport != null) {
                sportsTemp[tempSport.name] =
                    Sport(
                        tempSport.id,
                        tempSport.name,
                        tempSport.toString(),
                        true,
                        Level.valueOf(userSport.level!!)
                    )
            } else {
                sportsTemp[tempSport.name] =
                    Sport(tempSport.id, tempSport.name, tempSport.toString(), false, Level.NO_LEVEL)
            }

        } else {
            sportsTemp[tempSport.name] =
                Sport(tempSport.id, tempSport.name, tempSport.toString(), false, Level.NO_LEVEL)
        }
    }

    // fills the sportChips
    sportsInit()

    sportsTemp.values.forEach { sport ->
        setEditSportsField(sport)
    }

    // setup the chips listeners
    setupChipsListeners()
}

internal fun EditProfileFragment.setupChipsListeners() {
    // add listeners to the sports views
    for ((sportName, sportChips) in sports) {
        // register floating context menu for the actual level icons
        registerForContextMenu(sportChips.actualLevelChip)
        // remove long press listener
        sportChips.actualLevelChip.setOnLongClickListener(null)

        // add listener to the actual level chip
        sportChips.actualLevelChip.setOnClickListener {
            // update sport considered at the moment by the menu
            consideredSport = sportName

            // open the context menu to select a new sport level
            requireActivity().openContextMenu(sportChips.actualLevelChip)
        }

        // add listener to the sport chip
        sportChips.chip.setOnClickListener {
            sportChipListener(sportName)
        }
    }
}

internal fun EditProfileFragment.setupOnBackPressedCallback() {
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // (the information will be persistently saved in the onPause method)

            if (username.error == null) {
                // navigate back to show profile fragment
                navController.popBackStack()
            } else {
                showToasty("error", requireContext(), "Please fix the errors!")
            }
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner, // LifecycleOwner
        callback
    )
}

/* manage load/save from/into storage */

internal fun EditProfileFragment.initializeTempVariables() {
    firstNameTemp = vm.userFirstName.value ?: getString(R.string.first_name)
    lastNameTemp = vm.userLastName.value ?: getString(R.string.last_name)
    usernameTemp = vm.userUsername.value ?: getString(R.string.username)
    radioGenderCheckedTemp = Gender.valueOf(vm.userGender.value ?: getString(R.string.male_gender))
    ageTemp = vm.userAge.value ?: getString(R.string.user_age)
    locationTemp = vm.userLocation.value ?: getString(R.string.user_location)
    bioTemp = vm.userBio.value ?: getString(R.string.user_bio)
}

internal fun EditProfileFragment.loadPicturesFromInternalStorage() {
    /* manage profile and background picture */

    // retrieve profile picture and update it with the one uploaded by the user, if any
    getPictureFromInternalStorage(requireActivity().filesDir, "profilePicture.jpeg")?.let {
        profilePicture.setImageBitmap(it)
    }

    // retrieve background picture and update it with the one uploaded by the user, if any
    getPictureFromInternalStorage(
        requireActivity().filesDir,
        "backgroundProfilePicture.jpeg"
    )?.let {
        backgroundProfilePicture.setImageBitmap(it)
    }
}

internal fun EditProfileFragment.saveInformationOnStorage() {

    // update view model variables
    vm.setUserFirstName(firstName.text.toString())
    vm.setUserLastName(lastName.text.toString())
    vm.setUserUsername(username.text.toString())
    vm.setUserGender(radioGenderCheckedTemp.toString())
    vm.setUserAge(age.text.toString())
    vm.setUserLocation(location.text.toString())
    vm.setUserBio(bio.text.toString())

    // update user sports
    vm.setUserSports(sportsTemp.filter { it.value.selected }.map { it.value.toSportLevel() })

    // save profile and background pictures into the internal storage
    profilePictureBitmap?.let {
        savePictureOnInternalStorage(it, requireActivity().filesDir, "profilePicture.jpeg")
    }
    backgroundProfilePictureBitmap?.let {
        savePictureOnInternalStorage(
            it,
            requireActivity().filesDir,
            "backgroundProfilePicture.jpeg"
        )
    }

    if (username.error == null) {
        // update db user information
        vm.updateDbUserInformation(1)

        // showing feedback information
        showToasty("success", requireContext(), "Information correctly saved!")
    }
}

internal fun EditProfileFragment.textListenerInit(fieldName: String): TextWatcher {
    // implement and return the TextWatcher interface
    return object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            when (fieldName) {
                "firstName" -> {
                    firstNameTemp = firstName.text.toString()
                }

                "lastName" -> {
                    lastNameTemp = lastName.text.toString()
                }

                "username" -> {
                    usernameTemp = username.text.toString()
                }

                "age" -> {
                    ageTemp = age.text.toString()
                }

                "location" -> {
                    locationTemp = location.text.toString()
                }

                "bio" -> {
                    bioTemp = bio.text.toString()
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {
            when (fieldName) {
                "username" -> {
                    // check if the username already exists
                    vm.checkUsername(s.toString())

                    if (s.toString() == "") {
                        username.error = getString(R.string.username_empty_error)
                    }
                }
            }
        }
    }
}

/* start new Activities to get a new picture */

internal fun EditProfileFragment.openCamera() {
    // Creating a file object for the temporal image
    val imageFile = File.createTempFile("temp_profile_picture", ".jpeg", requireActivity().cacheDir)

    // Creating through a FileProvider the URI
    cameraUri = FileProvider.getUriForFile(
        requireContext(),
        "it.polito.mad.sportapp.fileprovider", imageFile
    )

    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)

    cameraActivityResultLauncher.launch(cameraIntent)
}

internal fun EditProfileFragment.openGallery() {
    // create intent and open phone gallery
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    galleryActivityResultLauncher.launch(galleryIntent)
}

internal fun galleryImagesPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE


/*----- SPORTS UTILITIES -----*/

// Fills sport chips and sport level lists, the chips in xml are named with the sport name
internal fun EditProfileFragment.sportsInit() {
    val sportsContainer = requireView().findViewById<ChipGroup>(R.id.sports_container)

    for ((sportName, sport) in sportsTemp) {
        // create the horizontal sport chip wrapper
        // (it will contain the sport chip and the level badge, if any)
        val sportChipWrapper = LinearLayout(requireContext()).apply {
            layoutParams = LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // width
                LinearLayout.LayoutParams.WRAP_CONTENT  // height
            )
            orientation = LinearLayout.HORIZONTAL
        }

        // create the Sport Chip
        val sportChip = createEditSportChip(sportsTemp[sportName]!!.displayName, sportChipWrapper)

        // create the actual Sport level chip
        val sportActualLevelChip =
            createEditSportLevelBadge(
                R.drawable.beginner_level_badge,
                sportChipWrapper,
                sport.level
            )

        // build sport hierarchy
        sportChipWrapper.addView(sportChip)
        sportChipWrapper.addView(sportActualLevelChip)

        // append everything to the sport container
        sportsContainer.addView(sportChipWrapper)

        // save views
        sports[sportName] = SportChips(sportName, sportChip, sportActualLevelChip)
    }
}

private fun EditProfileFragment.createEditSportChip(sportName: String, parent: ViewGroup): Chip {
    // inflate generic sport chip
    val sportChip = layoutInflater.inflate(R.layout.edit_profile_chip, parent, false) as Chip
    // customize it for the provided sport
    sportChip.text = sportName

    return sportChip
}

private fun EditProfileFragment.createEditSportLevelBadge(
    levelIconResource: Int,
    parent: ViewGroup,
    level: Level
): Chip {
    // inflate generic level badge
    val levelChip = layoutInflater.inflate(
        if (level == Level.INTERMEDIATE)
            R.layout.edit_profile_sport_level_chip_big
        else
            R.layout.edit_profile_sport_level_chip,
        parent,
        false
    ) as Chip

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
internal fun EditProfileFragment.sportChipListener(sportName: String) {
    val sport = sports[sportName]!!
    val sportTemp = sportsTemp[sportName]!!

    if (sportTemp.selected) { // this sport was already selected -> deselect it
        sportTemp.selected = false
        sportTemp.level = Level.NO_LEVEL    // reset level

        // hide actual level image
        sport.actualLevelChip.visibility = Chip.GONE
    } else {
        // update sport considered at the moment by the menu
        consideredSport = sportName

        // this sport was not selected -> open the context menu to choose the level
        requireActivity().openContextMenu(sport.actualLevelChip)

        // hide the checked icon on the sport badge
        sport.chip.isChecked = false
    }
}

internal fun EditProfileFragment.changeSportLevel(sportName: String, level: Level) {
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
    sportChips.actualLevelChip.setIconStartPaddingResource(
        if (level == Level.INTERMEDIATE)
            R.dimen.chip_icon_padding_intermediate
        else
            R.dimen.chip_icon_padding
    )

    sportChips.actualLevelChip.visibility = Chip.VISIBLE
}

// Set sports Edit fields and temporary values
private fun EditProfileFragment.setEditSportsField(sport: Sport) {
    if (sport.selected) {

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
