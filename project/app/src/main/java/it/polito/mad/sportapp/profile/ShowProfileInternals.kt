package it.polito.mad.sportapp.profile

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.material.chip.Chip
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.setProfilePictureSize
import it.polito.mad.sportapp.showToasty

// manage menu item selection
internal fun ShowProfileFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.profile_menu, menu)

            // change app bar's title
            actionBar?.title = "Profile"

            // change visibility of the show reservations menu item
            menu.findItem(R.id.edit_button).isVisible = true

            val menuHeight = actionBar?.height!!
            val profilePictureContainer =
                requireView().findViewById<ConstraintLayout>(R.id.profile_picture_container)
            val backgroundProfilePicture =
                requireView().findViewById<ImageView>(R.id.background_profile_picture)
            val profilePicture = requireView().findViewById<ImageView>(R.id.profile_picture)

            // set profile picture height 1/3 of the app view
            activity?.setProfilePictureSize(
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
                    //TODO: modify with edit profile fragment navigation
                    navController.navigate(R.id.action_showReservationsFragment_to_eventsListFragment)
                    true
                }

                else -> false
            }
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

internal fun ShowProfileFragment.viewsInit() {
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