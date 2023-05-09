package it.polito.mad.sportapp.reservation_management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
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
    internal lateinit var navController: NavController

    // params
    internal lateinit var currentMode: ReservationManagementMode
    internal var reservationId : Int? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        // determine if we are in 'create mode' or in 'edit mode'
        currentMode =
            if (arguments?.getString("mode") == "edit")
                ReservationManagementMode.EDIT_MODE
            else
                ReservationManagementMode.CREATE_MODE

        reservationId = arguments?.getInt("reservation_id")

        // modify activity action bar
        this.initMenu()

        // hide bottom bar during reservation management
        this.setupBottomBar()

        if (currentMode == ReservationManagementMode.EDIT_MODE) {
            val tv = requireActivity().findViewById<TextView>(R.id.tv)
            tv.text = "${tv.text} ${reservationId}"
        }

        // TODO
    }
}