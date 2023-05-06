package it.polito.mad.sportapp

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

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
        when (item.itemId) {
            R.id.reservations -> {
                navController.navigate(R.id.showReservationsFragment)
            }

            R.id.playgrounds -> {
                navController.navigate(R.id.playgroundAvailabilitiesFragment)
            }

            R.id.profile -> {
                // TODO
            }
            else -> throw Exception("An unexpected bottom bar item has been pressed")
        }

        bottomNavigationView.visibility = NavigationBarView.VISIBLE

        return true
 }

    /* app menu */

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // inflate and render the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.sport_app_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }
}