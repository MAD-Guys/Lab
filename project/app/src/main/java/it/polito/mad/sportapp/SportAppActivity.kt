package it.polito.mad.sportapp

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
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
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar)

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

        val currentFragment = navController.currentDestination?.id

        when (item.itemId) {

            R.id.reservations -> {
                if (currentFragment != R.id.showReservationsFragment) {
                    navController.navigate(R.id.showReservationsFragment)
                }
                return true
            }

            R.id.playgrounds -> {
                if (currentFragment != R.id.playgroundAvailabilitiesFragment) {
                    navController.navigate(R.id.playgroundAvailabilitiesFragment)
                }
                return true
            }

            R.id.profile -> {
                if (currentFragment != R.id.showProfileFragment) {
                    navController.navigate(R.id.showProfileFragment)
                }
                return true
            }

            else -> return false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_container_view)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}