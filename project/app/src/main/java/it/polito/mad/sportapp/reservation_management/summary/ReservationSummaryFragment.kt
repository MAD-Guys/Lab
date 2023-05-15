package it.polito.mad.sportapp.reservation_management.summary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.NewReservationEquipment
import it.polito.mad.sportapp.showToasty
import java.time.Duration
import java.time.LocalDateTime

@AndroidEntryPoint
class ReservationSummaryFragment : Fragment(R.layout.reservation_summary_view) {
    private val viewModel by viewModels<ReservationSummaryViewModel>()

    private lateinit var confirmReservationDialog: AlertDialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showToasty(
            "info",
            requireContext(),
            "reservation=${arguments?.getBundle("reservation")?.toString()}",
            Toasty.LENGTH_LONG
        )

        this.initConfirmReservationDialog()

        /* app bar and menu */
        this.initAppBar()
        this.initMenu()

        this.checkAndInitReservationData()


        // TODO
    }

    /* app bar and menu */

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
                        confirmReservationDialog.show()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /* Confirm reservation dialog */
    private fun initConfirmReservationDialog() {
        confirmReservationDialog = AlertDialog.Builder(requireContext())
            .setMessage("Do you want to confirm this reservation?")
            .setPositiveButton("YES") { _, _ ->
                val (newReservationId, error) = viewModel.permanentlySaveReservation()

                if (error == null && newReservationId != null) {
                    // * reservation correctly added/updated *

                    showToasty(
                        "success",
                        requireContext(),
                        "Reservation correctly saved with ID $newReservationId",
                        Toasty.LENGTH_LONG
                    )

                    // navigate back to the home
                    findNavController().popBackStack(R.id.showReservationsFragment, false)

                    // go to the new reservation detail
                    val params = bundleOf("id_event" to newReservationId)
                    findNavController().navigate(R.id.reservationDetailsFragment, params)
                }
                else if (error != null && newReservationId == null) {
                    // an error occurred during reservation update
                    showToasty(
                        "error",
                        requireContext(),
                        error.message,
                        Toasty.LENGTH_LONG
                    )
                }
            }
            .setNegativeButton("NO") { d, _ -> d.cancel() }
            .create()
    }

    /* reservation */

    private fun checkAndInitReservationData() {
        val reservationBundle = arguments?.getBundle("reservation")

        if(reservationBundle == null) {
            showToasty(
                "error",
                requireContext(),
                "Error: reservation data absent"
            )
            return
        }

        val reservationId = reservationBundle.getInt("reservation_id")
        val startSlotStr = reservationBundle.getString("start_slot")
        val endSlotStr = reservationBundle.getString("end_slot")
        val slotDurationMins = reservationBundle.getInt("slot_duration_mins")
        val playgroundId = reservationBundle.getInt("playground_id")
        val playgroundName = reservationBundle.getString("playground_name")
        val sportId = reservationBundle.getInt("sport_id")
        val sportName = reservationBundle.getString("sport_name")
        val sportCenterId = reservationBundle.getInt("sport_center_id")
        val sportCenterName = reservationBundle.getString("sport_center_name")
        val equipmentsBundle = reservationBundle.getBundle("equipments")

        val inputErrors = mutableListOf<String>()

        if (startSlotStr == null)
            inputErrors.add("start_slot")

        if(endSlotStr == null)
            inputErrors.add("end_slot")

        if(slotDurationMins == 0)
            inputErrors.add("slot_duration_mins")

        if(playgroundId == 0)
            inputErrors.add("playground_id")

        if (playgroundName == null)
            inputErrors.add("playground_name")

        if(sportId == 0)
            inputErrors.add("sport_id")

        if(sportName == null)
            inputErrors.add("sport_name")

        if(sportCenterId == 0)
            inputErrors.add("sport_center_id")

        if(sportCenterName == null)
            inputErrors.add("sport_center_name")

        if(equipmentsBundle == null)
            inputErrors.add("equipments")

        if(inputErrors.isNotEmpty()) {
            // some input fields is/are missing
            showToasty(
                "error",
                requireContext(),
                inputErrors.joinToString(separator=", ",
                    prefix="Error: the following input fields are missing: "),
                Toasty.LENGTH_LONG
            )
        }

        // * all data are available here *
        val startTime = LocalDateTime.parse(startSlotStr)
        val endTime = LocalDateTime.parse(endSlotStr).plus(Duration.ofMinutes(slotDurationMins.toLong()))
        val selectedEquipments = mutableListOf<NewReservationEquipment>()

        equipmentsBundle!!.keySet().forEach { equipmentId ->
            val equipment = equipmentsBundle.getBundle(equipmentId)!!

            val newEquipment = NewReservationEquipment(
                equipment.getInt("equipment_id"),
                equipment.getString("equipment_name")!!,
                equipment.getInt("selected_quantity"),
                equipment.getFloat("unit_price")
            )

            if (newEquipment.equipmentId == 0 || newEquipment.selectedQuantity == 0 || newEquipment.unitPrice == 0f) {
                showToasty("error", requireContext(), "Error in fields for an equipment")
                return
            }

            selectedEquipments.add(newEquipment)
        }

        // create reservation object
        val newReservation = NewReservation(
            reservationId,
            startTime,
            endTime,
            playgroundId,
            playgroundName!!,
            sportId,
            sportName!!,
            sportCenterId,
            sportCenterName!!,
            selectedEquipments
        )

        // save it
        viewModel.setReservation(newReservation)
    }
}