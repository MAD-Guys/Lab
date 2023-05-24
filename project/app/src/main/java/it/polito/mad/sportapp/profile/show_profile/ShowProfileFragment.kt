package it.polito.mad.sportapp.profile.show_profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.application_utilities.getPictureFromInternalStorage
import it.polito.mad.sportapp.profile.ProfileViewModel
import it.polito.mad.sportapp.profile.Sport

@AndroidEntryPoint
class ShowProfileFragment : Fragment(R.layout.fragment_show_profile) {
    // User info views
    internal lateinit var firstName: TextView
    internal lateinit var lastName: TextView
    internal lateinit var username: TextView
    internal lateinit var gender: TextView
    internal lateinit var age: TextView
    internal lateinit var location: TextView
    internal lateinit var bio: TextView

    // Profile picture views
    internal lateinit var profilePicture: ImageView
    internal lateinit var backgroundProfilePicture: ImageView

    // Button views
    internal lateinit var addFriendButton: Button
    internal lateinit var messageButton: Button
    internal lateinit var logoutButton: Button

    // fragment dialog
    internal lateinit var exitDialog: AlertDialog

    // FireSport views
    internal lateinit var noSportsTextView: TextView
    internal lateinit var sportsContainer: ChipGroup
    internal var sportChips: MutableMap<String, Chip> = HashMap()

    // sport data
    internal var sportData: MutableMap<String, Sport> = HashMap()

    // action bar
    internal var actionBar: ActionBar? = null

    // navigation controller
    internal lateinit var navController: NavController

    // show profile view model
    internal val vm by activityViewModels<ProfileViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // initialize navigation controller
        navController = findNavController()

        // initialize menu
        menuInit()

        // initialize exit dialog
        exitDialogInit()

        // setup layout views
        viewsSetup()

        // setup layout observers
        observersSetup()

        // initialize buttons
        buttonsInit()

        // inflate achievements layout
        inflateAchievementsLayout()

        // setup bottom bar
        setupBottomBar()

    }

    override fun onResume() {
        super.onResume()

        /*  The information is retrieved and showed in onResume() because  *
         *  it has to be refreshed after resuming the fragment */

        // set sports inflated flag to false
        vm.setSportsInflated(false)

        // retrieve user information from db
        vm.loadUserInformationFromDb(1)

        // get all sports from db
        vm.loadSportsFromDb()

        // retrieve profile and background picture from the internal storage
        val profilePictureBitmap =
            getPictureFromInternalStorage(requireActivity().filesDir, "profilePicture.jpeg")
        val backgroundProfilePictureBitmap =
            getPictureFromInternalStorage(
                requireActivity().filesDir,
                "backgroundProfilePicture.jpeg"
            )

        // update profile and background picture with the ones uploaded by the user, if any
        profilePictureBitmap?.let { profilePicture.setImageBitmap(it) }
        backgroundProfilePictureBitmap?.let { backgroundProfilePicture.setImageBitmap(it) }
    }
}