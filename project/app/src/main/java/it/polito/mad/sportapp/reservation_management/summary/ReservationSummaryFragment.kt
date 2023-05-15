package it.polito.mad.sportapp.reservation_management.summary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.showToasty

@AndroidEntryPoint
class ReservationSummaryFragment : Fragment(R.layout.reservation_summary_view) {
    @Suppress("unused")
    private val viewModel by viewModels<ReservationSummaryViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showToasty(
            "info",
            requireContext(),
            "reservation=${arguments?.getBundle("reservation")?.toString()}",
            Toasty.LENGTH_LONG
        )

        /* app bar and menu */
        this.initAppBar()
        this.initMenu()

        // TODO
    }

    private fun initAppBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        actionBar?.let {
            // show back arrow and the right title
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
            it.title = "Reservation summary"
        }
    }

    private fun initMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(
                    R.menu.reservation_summary_menu,
                    menu
                )
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save_reservation_button -> {
                        // TODO show dialog message to confirm the save
                        showToasty("info", requireContext(), "are you sure? etc")
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}