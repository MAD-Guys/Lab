package it.polito.mad.sportapp.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.SportAppViewModel
import it.polito.mad.sportapp.application_utilities.checkIfUserIsLoggedIn
import it.polito.mad.sportapp.notifications.manageInvitationNotification

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    // view model
    internal lateinit var vm: SportAppViewModel

    // login variables
    internal val logInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
        this::onSignInResult
    )

    // login views
    private lateinit var iconLauncherImageView: ImageView
    private lateinit var welcomeText: TextView
    internal lateinit var googleSignInButton: LinearLayout

    // action bar
    internal var actionBar: ActionBar? = null

    // navigation controller
    internal lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize navigation controller
        navController = findNavController()

        // check if user is already logged in
        if (checkIfUserIsLoggedIn()) {

            //TODO: setup firestore db properly and uncomment the following lines of code
            // check if user already exists in firestore db
            /*
            if (vm.checkIfUserAlreadyExists(FirebaseAuth.getInstance().currentUser!!.uid)) {
                vm.addUserOnDb()
            }*/

            val activityIntent = requireActivity().intent

            // check if the activity has an intent
            if (activityIntent != null) {
                if (activityIntent.action == "NEW_INVITATION") {
                    manageInvitationNotification(activityIntent, navController)
                } else {
                    // navigate to showReservations fragment
                    navController.navigate(R.id.showReservationsFragment)
                }
            } else {
                // navigate to showReservations fragment
                navController.navigate(R.id.showReservationsFragment)
            }
        }

        // initialize view model
        vm = ViewModelProvider(requireActivity())[SportAppViewModel::class.java]

        // setup icon image view
        iconLauncherImageView = requireView().findViewById(R.id.icon_launcher_image_view)

        // setup welcome text
        welcomeText = requireView().findViewById(R.id.welcome_text)
        val welcomeString = "Welcome\nto\n${getString(R.string.app_name)}\nApp!"
        welcomeText.text = welcomeString

        // setup google sign in button
        googleButtonInit()

        // setup back button
        setupOnBackPressedCallback()

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // initialize menu
        menuInit()

        // setup bottom bar
        setupBottomBar()


    }

    override fun onStart() {
        super.onStart()

        // create and start icon animation
        val animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.app_logo_rotate_animation)
        iconLauncherImageView.startAnimation(animation)
    }
}