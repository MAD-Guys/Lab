package it.polito.mad.sportapp.profile.show_profile

import android.content.res.Configuration
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.profile.Level
import it.polito.mad.sportapp.profile.Sport
import it.polito.mad.sportapp.profile.extendedNameOf
import it.polito.mad.sportapp.setProfilePictureSize
import it.polito.mad.sportapp.showToasty

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

                else -> false
            }
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
}

internal fun ShowProfileFragment.observersSetup() {

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
        age.text = it.toString()
    }

    // user location observer
    vm.userLocation.observe(viewLifecycleOwner) {
        location.text = it
    }

    // user bio observer
    vm.userBio.observe(viewLifecycleOwner) {
        bio.text = it
    }
}

internal fun ShowProfileFragment.buttonsInit() {
    // retrieve buttons and set their callbacks
    addFriendButton = requireView().findViewById(R.id.button_add_friend)
    messageButton = requireView().findViewById(R.id.button_message)

    addFriendButton.setOnClickListener {
        showToasty("info", requireContext(), "Add friend button clicked!!!")
    }

    messageButton.setOnClickListener {
        showToasty("info", requireContext(), "Message button clicked!!!")
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

internal fun ShowProfileFragment.createSportChip(sport: Sport, parent: ViewGroup): Chip {
    val chip = layoutInflater.inflate(R.layout.show_profile_chip, parent, false) as Chip

    chip.apply {
        activity?.setVisible(sport.selected)  // !!!
        text = extendedNameOf(sport.name) // !!!
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

internal fun ShowProfileFragment.loadHardcodedSports(
    vararg hardcodedSports: Sport,
    parent: ViewGroup
) {
    hardcodedSports.forEach {
        // create chip view
        val sportChip = createSportChip(it, parent)

        // save chip and sport info
        sportChips[it.name] = sportChip
        sportData[it.name] = it
    }
}