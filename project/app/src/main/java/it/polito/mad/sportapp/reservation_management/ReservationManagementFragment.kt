package it.polito.mad.sportapp.reservation_management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R

@AndroidEntryPoint
class ReservationManagementFragment : Fragment(R.layout.reservation_management_view) {
    enum class ReservationManagementMode {
        CREATE_MODE, EDIT_MODE
    }

    internal val viewModel by viewModels<ReservationManagementViewModel>()
    internal lateinit var currentMode: ReservationManagementMode
    internal lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        // determine if we are in 'create mode' or in 'edit mode'
        currentMode =
            if (arguments?.getString("mode") == "edit")
                ReservationManagementMode.EDIT_MODE
            else
                ReservationManagementMode.CREATE_MODE

        // modify activity action bar
        this.initMenu()

        // TODO
    }

    override fun onResume() {
        super.onResume()

        // hide bottom bar during reservation management
        this.setupBottomBar()
    }
}