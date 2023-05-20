package it.polito.mad.sportapp.notifications

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
internal fun NotificationsFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.notifications_menu, menu)

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(false)
                it.title = "My Notifications"
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // handle the menu selection
            return true
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

/* bottom bar */
internal fun NotificationsFragment.setupBottomBar() {
    // show bottom bar
    val bottomBar =
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)

    bottomBar.visibility = View.VISIBLE

    // set the right selected button
    bottomBar.menu.findItem(R.id.notifications).isChecked = true
}