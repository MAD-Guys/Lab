package it.polito.mad.sportapp.notification_details

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.polito.mad.sportapp.R

// manage menu item selection
internal fun NotificationDetailsFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.notification_details_menu, menu)

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
                it.title = "Notification Details"
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // handle the menu selection
            return false
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

/* bottom bar */
internal fun NotificationDetailsFragment.setupBottomBar() {
    // hide bottom bar
    val bottomBar =
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)

    bottomBar.visibility = View.GONE
}