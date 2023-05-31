package it.polito.mad.sportapp.profile.show_profile

import android.content.res.Configuration
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.SportAppViewModel
import it.polito.mad.sportapp.entities.Achievement
import it.polito.mad.sportapp.entities.SportLevel
import it.polito.mad.sportapp.application_utilities.logOut
import it.polito.mad.sportapp.profile.Level
import it.polito.mad.sportapp.profile.ProfileSport
import it.polito.mad.sportapp.application_utilities.setProfilePictureSize
import it.polito.mad.sportapp.application_utilities.showToasty

// manage menu item selection
internal fun ShowProfileFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.profile_menu, menu)

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(false)
                it.title = "Profile"
            }

            val menuHeight = actionBar?.height!!
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
                R.id.edit_button -> {
                    navController.navigate(R.id.action_showProfileFragment_to_editProfileFragment)
                    true
                }

                R.id.logout_button_menu -> {
                    exitDialog.show()
                    true
                }

                else -> false
            }
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

/* Exit and logout dialog */
internal fun ShowProfileFragment.exitDialogInit() {
    exitDialog = AlertDialog.Builder(requireContext())
        .setMessage("Do you want logout?")
        .setPositiveButton("YES") { _, _ ->

            // get activity view model instance
            val activityViewModel =
                ViewModelProvider(requireActivity())[SportAppViewModel::class.java]

            // set user logged out
            activityViewModel.setUserLoggedIn(false)

            // logout and navigate back to the login fragment
            logOut(
                requireContext(),
                navController,
                R.id.action_showProfileFragment_to_loginFragment,
            )
        }
        .setNegativeButton("NO") { d, _ -> d.cancel() }
        .create()
}

internal fun ShowProfileFragment.viewsSetup() {
    // retrieve user info and picture views
    firstName = requireView().findViewById(R.id.first_name)
    lastName = requireView().findViewById(R.id.last_name)
    username = requireView().findViewById(R.id.username)
    gender = requireView().findViewById(R.id.user_gender)
    age = requireView().findViewById(R.id.user_age)
    location = requireView().findViewById(R.id.user_location)
    bio = requireView().findViewById(R.id.user_bio)
    profilePicture = requireView().findViewById(R.id.profile_picture)
    backgroundProfilePicture = requireView().findViewById(R.id.background_profile_picture)

    // retrieve sports views
    noSportsTextView =
        requireView().findViewById(R.id.no_sports_selected_text_view)

    sportsContainer = requireView().findViewById(R.id.sports_container)
}

internal fun ShowProfileFragment.observersSetup() {

    // user profile picture observer
    vm.userProfilePicture.observe(viewLifecycleOwner) {
        profilePicture.setImageBitmap(it)
    }

    // user background profile picture observer
    vm.userBackgroundProfilePicture.observe(viewLifecycleOwner) {
        backgroundProfilePicture.setImageBitmap(it)
    }

    // user first name observer
    vm.userFirstName.observe(viewLifecycleOwner) {
        firstName.text = it
    }

    // user last name observer
    vm.userLastName.observe(viewLifecycleOwner) {
        lastName.text = it
    }

    // user username observer
    vm.userUsername.observe(viewLifecycleOwner) {
        username.text = it
    }

    // user gender observer
    vm.userGender.observe(viewLifecycleOwner) {
        gender.text = it
    }

    // user age observer
    vm.userAge.observe(viewLifecycleOwner) {
        age.text = it
    }

    // user location observer
    vm.userLocation.observe(viewLifecycleOwner) {
        location.text = it
    }

    // user bio observer
    vm.userBio.observe(viewLifecycleOwner) {

        val bioDivider = requireView().findViewById<View>(R.id.bio_divider)
        val bioContainer = requireView().findViewById<LinearLayout>(R.id.bio_container)

        // hide bio if empty
        if (it.isEmpty()) {
            bioDivider.visibility = View.GONE
            bioContainer.visibility = View.GONE
            return@observe
        } else {
            bioDivider.visibility = View.VISIBLE
            bioContainer.visibility = View.VISIBLE
            bio.text = it
        }
    }

    vm.sportsList.observe(viewLifecycleOwner) {
        if (!vm.sportsInflated && vm.userSportsLoaded) {
            if (vm.userSports.value!!.isNotEmpty()) {
                noSportsTextView.visibility = View.GONE
                setupSports(vm.userSports.value!!)
            } else {
                // show default message
                noSportsTextView.visibility = View.VISIBLE
            }
        }
    }

    vm.userSports.observe(viewLifecycleOwner) {
        if (!vm.sportsInflated && vm.sportsListLoaded) {
            if (it.isNotEmpty()) {
                noSportsTextView.visibility = View.GONE
                setupSports(it)
            } else {
                // show default message
                noSportsTextView.visibility = View.VISIBLE
            }
        }
    }

    vm.userAchievements.observe(viewLifecycleOwner) {
        if (it.isNotEmpty()) {
            updateAchievements(it)
        }
    }

    // * error/success observers *

    // repository error getting user information -> show an error toast
    vm.getUserError.observe(viewLifecycleOwner) {
        if(it != null){ // it can be an Error, or null
            showToasty("error", requireContext(), it.message())
        }
    }

    // repository error saving new user information -> show an error toast and stay here
    vm.updateUserError.observe(viewLifecycleOwner) {
        if(it != null){ // it can be an Error, or null
            showToasty("error", requireContext(), it.message())
        }
    }

    // information saved successfully -> show a success toast
    vm.updateUserSuccess.observe(viewLifecycleOwner) {
        if(it){
            // showing feedback information
            showToasty("success", requireContext(), "Information correctly saved!")
            vm.clearSuccess()
        }
    }
}

internal fun ShowProfileFragment.setupSports(userSports: List<SportLevel>) {
    /* manage sports */

    // clean sports container amd sportChips
    sportsContainer.removeAllViews()
    sportChips.clear()

    // create sports chips
    userSports.forEach {

        val sportId: String? = getDbSportId(it.sport!!)
        val sportString: String = getDbSportName(it.sport) ?: it.sport

        if (sportId != null) {
            val sport = ProfileSport.from(sportId, it.sport, sportString, it.level!!)
            val sportChip = createSportChip(sport, sportsContainer)

            sportChips[it.sport] = sportChip
            sportData[it.sport] = sport
        }
    }

    // display sports in decreasing order of level
    sportChips.asSequence().sortedByDescending { (sportName, _) ->
        sportData[sportName]!!.level
    }.forEach { (_, chip) ->
        sportsContainer.addView(chip)
    }

    vm.setSportsInflated(true)
}

private fun ShowProfileFragment.getDbSportId(sportName: String): String? {

    vm.sportsList.value?.let { sportList ->
        val sport = sportList.find { it.name == sportName }
        sport?.let {
            return it.id
        }
    }

    return null
}

private fun ShowProfileFragment.getDbSportName(sportName: String): String? {

    vm.sportsList.value?.let { sportList ->
        val sport = sportList.find { it.name == sportName }
        sport?.let {
            return it.printWithEmoji(onTheLeft = false)
        }
    }

    return null
}

internal fun ShowProfileFragment.buttonsInit() {
    // retrieve buttons and set their callbacks
    addFriendButton = requireView().findViewById(R.id.button_add_friend)
    messageButton = requireView().findViewById(R.id.button_message)
    logoutButton = requireView().findViewById(R.id.logout_button)

    addFriendButton.setOnClickListener {
        showToasty("info", requireContext(), "Add friend button clicked!!!")
    }

    messageButton.setOnClickListener {
        showToasty("info", requireContext(), "Message button clicked!!!")
    }

    logoutButton.setOnClickListener {
        exitDialog.show()
    }
}

internal fun ShowProfileFragment.inflateAchievementsLayout() {
    // retrieve achievements container
    val achievementsContainer =
        requireView().findViewById<LinearLayout>(R.id.achievements_container)

    // inflate achievements
    val sportsAchievements =
        layoutInflater.inflate(
            R.layout.matches_achievements_layout,
            achievementsContainer,
            false
        )
    val matchesAchievements =
        layoutInflater.inflate(
            R.layout.sports_achievements_layout,
            achievementsContainer,
            false
        )

    // add achievements to parent container
    achievementsContainer.addView(matchesAchievements)
    achievementsContainer.addView(sportsAchievements)
}

internal fun ShowProfileFragment.updateAchievements(achievementsMap: Map<Achievement, Boolean>) {

    achievementsMap.forEach { (achievement, boolean) ->

        val achievementLayout: ConstraintLayout

        when (achievement) {
            // one sport achievement
            Achievement.AtLeastOneSport -> {
                achievementLayout = requireView().findViewById(R.id.at_least_one_sport_layout)

                if (boolean) {
                    achievementLayout.findViewById<ImageView>(R.id.at_least_one_sport_achievement)
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.one_sport_achievement
                            )
                        )
                }
            }

            // five sports achievement
            Achievement.AtLeastFiveSports -> {
                achievementLayout = requireView().findViewById(R.id.at_least_five_sports_layout)

                if (boolean) {
                    achievementLayout.findViewById<ImageView>(R.id.at_least_five_sports_achievement)
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.five_sports_achievement
                            )
                        )
                }
            }

            // all sports achievement
            Achievement.AllSports -> {
                achievementLayout = requireView().findViewById(R.id.all_sports_layout)

                if (boolean) {
                    achievementLayout.findViewById<ImageView>(R.id.all_sports_achievement)
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.all_sports_achievement
                            )
                        )
                }
            }

            // three matches achievement
            Achievement.AtLeastThreeMatches -> {
                achievementLayout = requireView().findViewById(R.id.at_least_three_matches_layout)

                if (boolean) {
                    achievementLayout.findViewById<ImageView>(R.id.at_least_three_matches_achievement)
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.three_matches_achievement
                            )
                        )
                }
            }

            // ten matches achievement
            Achievement.AtLeastTenMatches -> {
                achievementLayout = requireView().findViewById(R.id.at_least_ten_matches_layout)

                if (boolean) {
                    achievementLayout.findViewById<ImageView>(R.id.at_least_ten_matches_achievement)
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ten_matches_achievement
                            )
                        )
                }
            }

            // twenty five matches achievement
            Achievement.AtLeastTwentyFiveMatches -> {
                achievementLayout =
                    requireView().findViewById(R.id.at_least_twenty_five_matches_layout)

                if (boolean) {
                    achievementLayout.findViewById<ImageView>(R.id.at_least_twenty_five_matches_achievement)
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.twenty_five_match_achievement
                            )
                        )
                }
            }
        }
    }
}

/* bottom bar */
internal fun ShowProfileFragment.setupBottomBar() {
    // show bottom bar
    val bottomBar = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)

    // check if the device is in portrait or landscape mode
    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        bottomBar.visibility = View.GONE
    else {
        bottomBar.visibility = View.VISIBLE
    }

    // set the right selected button
    bottomBar.menu.findItem(R.id.profile).isChecked = true
}

internal fun ShowProfileFragment.createSportChip(sport: ProfileSport, parent: ViewGroup): Chip {
    val chip = layoutInflater.inflate(R.layout.show_profile_chip, parent, false) as Chip

    chip.apply {
        activity?.setVisible(sport.selected)
        text = sport.displayName
        // set level characteristics
        when (sport.level) {
            Level.BEGINNER -> {
                setChipIconResource(R.drawable.beginner_level_badge)
                setChipStrokeColorResource(R.color.beginner_badge_blue)
                setTextColor(ContextCompat.getColor(context, R.color.beginner_badge_blue))
            }

            Level.INTERMEDIATE -> {
                setChipIconResource(R.drawable.intermediate_level_badge)
                setChipStrokeColorResource(R.color.intermediate_badge_blue)
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.intermediate_badge_blue
                    )
                )
                setChipIconSizeResource(R.dimen.chip_icon_size_big)
                setIconStartPaddingResource(R.dimen.chip_icon_padding_intermediate)
            }

            Level.EXPERT -> {
                setChipIconResource(R.drawable.expert_level_badge)
                setChipStrokeColorResource(R.color.expert_badge_blue)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.expert_badge_blue))
            }

            Level.PRO -> {
                setChipIconResource(R.drawable.pro_level_badge)
                setChipStrokeColorResource(R.color.pro_badge_grey)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.pro_badge_grey))
            }

            else -> throw RuntimeException("Unexpected selected sport with NO_LEVEL!")
        }
    }

    return chip
}