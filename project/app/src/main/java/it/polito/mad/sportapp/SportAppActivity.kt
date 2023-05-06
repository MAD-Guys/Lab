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

        // configure menu
        menuInit()
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

    private fun menuInit() {
        // add menu items without overriding methods in the Activity
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // inflate the menu
                menuInflater.inflate(R.menu.sport_app_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.events_list_button -> {
                        navController.navigate(R.id.action_showReservationsFragment_to_eventsListFragment)
                        true
                    }

                    else -> false
                }
            }
        })
    }

}