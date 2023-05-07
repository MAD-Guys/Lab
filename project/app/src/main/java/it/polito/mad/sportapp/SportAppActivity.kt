package it.polito.mad.sportapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SportAppActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    private lateinit var bottomNavigationView: NavigationBarView
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set sport app content view
        setContentView(R.layout.activity_sport_app)

        // set english as default language
        setApplicationLocale(this, "en", "EN")

        /* bottom bar */

        // initialize bottom navigation view
        bottomNavigationView = findViewById(R.id.bottom_navigation_menu)

        // initialize navigation controller
        navController = (
                supportFragmentManager
                    .findFragmentById(R.id.fragment_container_view) as NavHostFragment
                ).navController

        // set bottom navigation bar listener
        bottomNavigationView.setOnItemSelectedListener(this)

        // configure toasts appearance
        toastyInit()

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val previousFragment = navController.previousBackStackEntry?.destination?.id

        when {
            (item.itemId == R.id.reservations && previousFragment == R.id.playgroundAvailabilitiesFragment) -> {
                navController.navigate(R.id.action_playgroundAvailabilitiesFragment_to_showReservationsFragment)
            }

            // TODO: uncomment and substitute with the show profile fragment and action
            /*(item.itemId == R.id.reservations && previousFragment == R.id.showProfileFragment) -> {
                navController.navigate(R.id.action_showProfileFragment_to_showReservationsFragment)
            }*/

            (item.itemId == R.id.playgrounds && previousFragment == R.id.showReservationsFragment) -> {
                navController.navigate(R.id.action_showReservationsFragment_to_playgroundAvailabilitiesFragment)
            }

            // TODO: uncomment and substitute with the show profile fragment and action
            /*(item.itemId == R.id.playgrounds && previousFragment == R.id.showProfileFragment) -> {
                navController.navigate(R.id.action_showProfileFragment_to_playgroundAvailabilitiesFragment)
            }*/

            /*
            (item.itemId == R.id.profile && previousFragment == R.id.showReservationsFragment) -> {
                navController.navigate(R.id.action_showReservationsFragment_to_showProfileFragment)
            }

            (item.itemId == R.id.profile && previousFragment == R.id.playgroundAvailabilitiesFragment) -> {
                navController.navigate(R.id.action_playgroundAvailabilitiesFragment_to_showProfileFragment)
            }*/

            else -> throw Exception("An unexpected bottom bar item has been pressed")
        }

        bottomNavigationView.visibility = NavigationBarView.VISIBLE

        return true
    }



}