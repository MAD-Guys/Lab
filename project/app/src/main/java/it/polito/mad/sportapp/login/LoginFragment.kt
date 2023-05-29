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
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.SportAppViewModel
import it.polito.mad.sportapp.application_utilities.checkIfUserIsLoggedIn
import it.polito.mad.sportapp.model.FireRepository
import it.polito.mad.sportapp.notifications.manageNotification

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    internal val iRepository = FireRepository()

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

        // initialize view model
        vm = ViewModelProvider(requireActivity())[SportAppViewModel::class.java]

        // initialize navigation controller
        navController = findNavController()

        // check if user is already logged in
        if (checkIfUserIsLoggedIn()) {
            // check if user already exists in database and insert into firestore db if user does not exist
            vm.checkIfUserAlreadyExists(FirebaseAuth.getInstance().currentUser!!.uid)

            manageNotification(requireActivity().intent, navController)
        }

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