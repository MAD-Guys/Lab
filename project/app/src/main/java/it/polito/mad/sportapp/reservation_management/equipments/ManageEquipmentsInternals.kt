package it.polito.mad.sportapp.reservation_management.equipments

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.showToasty

internal fun ManageEquipmentsFragment.initAppBar() {
    val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

    actionBar?.let {
        // show back arrow and the right title
        it.setDisplayHomeAsUpEnabled(true)
        it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        it.title = "Reservation equipments"
    }
}

internal fun ManageEquipmentsFragment.initMenu() {
    requireActivity().addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(
                R.menu.manage_equipments_menu,
                menu
            )
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.save_equipments_button -> {
                    // TODO: navigate to reservation summary
                    showToasty("info", requireContext(), "Go to reservation summary")

                    true
                }
                else -> false
            }
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}
