package it.polito.mad.sportapp.login

import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.application_utilities.showToasty

// manage menu item selection
internal fun LoginFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.login_menu, menu)

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(false)
                it.title = ""
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // handle the menu selection
            return false
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

/* manage back button */
internal fun LoginFragment.setupOnBackPressedCallback() {
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // finish activity
            requireActivity().finish()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner, // LifecycleOwner
        callback
    )
}

/* google sign in button */
internal fun LoginFragment.googleButtonInit() {

    googleSignInButton =
        requireView().findViewById(R.id.google_sign_in_button_container)

    googleSignInButton.clipToOutline = true

    googleSignInButton.setOnClickListener {
        createSignInIntent()
    }
}

/* bottom bar */
internal fun LoginFragment.setupBottomBar() {
    // hide bottom bar
    val bottomBar = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)

    bottomBar.visibility = View.GONE
}

/* log in */
internal fun LoginFragment.createSignInIntent() {

    // Choose authentication providers
    val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build(),
    )

    // Create and launch log-in intent
    val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .setTheme(R.style.Theme_sportapp) // set sportapp theme
        .build()

    logInLauncher.launch(signInIntent)
}

internal fun LoginFragment.onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
    val response = result.idpResponse

    if (result.resultCode == RESULT_OK) {
        // user successfully logged in
        val user = FirebaseAuth.getInstance().currentUser

        // update UI
        updateUI(user)

        // print log
        Log.d(TAG, "Log in successful")
    } else {
        // log in failed
        Log.e(TAG, "Log in failed", response?.error)
        updateUI(null)
    }
}

/* update UI */
internal fun LoginFragment.updateUI(currentUser: FirebaseUser?) {
    if (currentUser != null) {
        // user is logged in
        // show success message
        showToasty("success", requireContext(), "Login successfully done!")

        // navigate to show reservations fragment
        navController.navigate(R.id.showReservationsFragment)
    } else {
        // user is not logged in
        // show error message
        showToasty("error", requireContext(), "Something went wrong,\nplease try again!")
    }
}
