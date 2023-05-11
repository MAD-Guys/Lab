package it.polito.mad.sportapp.profile.show_profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.getPictureFromInternalStorage
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

    // Sport views
    private lateinit var sportChips: MutableMap<String, Chip>

    // sport data
    private lateinit var sportData: MutableMap<String, Sport>

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
         *  it has to be refreshed after saving in edit mode  */

        // retrieve user information from db
        vm.loadUserInformationFromDb(1)

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

        /* manage sports */
        sportChips = HashMap()
        sportData = HashMap()

        // retrieve and clean sports container
        val sportsContainer = requireView().findViewById<ChipGroup>(R.id.sports_container)
        sportsContainer.removeAllViews()

        // load the (already) selected sports by the user
        vm.userSports.value?.let { userSports ->

            if (userSports.isEmpty()) {
                // show default message
                val noSportsTextView =
                    requireView().findViewById<TextView>(R.id.no_sports_selected_text_view)
                noSportsTextView.visibility = View.VISIBLE
            } else {
                // create sports chips
                userSports.forEach {

                    val sport = Sport.from(it.sport!!, it.level!!)

                    val sportChip = createSportChip(sport, sportsContainer)

                    sportChips[it.sport] = sportChip
                    sportData[it.sport] = sport
                }
            }
        }

        // display sports in decreasing order of level
        sportChips.asSequence().sortedByDescending { (sportName, _) ->
            sportData[sportName]!!.level
        }.forEach { (_, chip) ->
            sportsContainer.addView(chip)
        }
    }
}