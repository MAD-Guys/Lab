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

        // TODO:
        //  - retrieve all the possible equipments for that playground (name + availableQty)
        //  - if reservation id exists, retrieve the existing equipments (if any) for that reservation
        //  - if any, increments/add those equipments quantities to the retrieved ones,
        //   and show those equipments in the view
        //  - attach onClickListener to each qty button
        //  - attach menu containing available equipments to the addEquipment button
        //  - attach go to reservation summary
    }
}