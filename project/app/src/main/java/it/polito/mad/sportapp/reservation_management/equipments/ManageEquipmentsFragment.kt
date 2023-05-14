package it.polito.mad.sportapp.reservation_management.equipments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import es.dmoral.toasty.Toasty
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.showToasty

class ManageEquipmentsFragment : Fragment(R.layout.manage_equipments_view) {
    private val viewModel by viewModels<ManageEquipmentsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showToasty(
            "info",
            requireContext(),
            arguments.toString(),
            Toasty.LENGTH_LONG
        )

        /* app bar and menu */
        this.initAppBar()
        this.initMenu()

        // TODO
    }

}