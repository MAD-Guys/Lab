package it.polito.mad.sportapp.reservation_management

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.reservation_management.ReservationManagementFragment.ReservationManagementMode.*
import it.polito.mad.sportapp.showToasty

/* app bar*/
internal fun ReservationManagementFragment.initMenu() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.reservation_management_menu, menu)

            // setup action bar
            val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
                it.title = when (currentMode) {
                    EDIT_MODE -> "Edit Reservation"
                    CREATE_MODE -> "Create Reservation"
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
            R.id.confirm_button -> {
                // TODO
                showToasty("info", requireContext(), "Information saved")
                true
            }
            else -> false
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

/* bottom bar */
internal fun ReservationManagementFragment.setupBottomBar() {
    // hide bottom bar
    val bottomBar = requireActivity().findViewById<View>(R.id.bottom_navigation_bar)
    bottomBar.visibility = View.GONE
}
