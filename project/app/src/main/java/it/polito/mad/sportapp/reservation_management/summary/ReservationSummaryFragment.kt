package it.polito.mad.sportapp.reservation_management.summary

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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
import it.polito.mad.sportapp.entities.NewReservationEquipment
import it.polito.mad.sportapp.application_utilities.showToasty
import it.polito.mad.sportapp.entities.NewReservation
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class ReservationSummaryFragment : Fragment(R.layout.reservation_summary_view) {
    private val viewModel by viewModels<ReservationSummaryViewModel>()

    // fragment dialogs
    private lateinit var confirmReservationDialog: AlertDialog
    private lateinit var deleteReservationDialog: AlertDialog

    // fragment views
    private lateinit var summarySportEmoji: TextView
    private lateinit var summarySportCenterName: TextView
    private lateinit var summaryPlaygroundName: TextView
    private lateinit var summarySportName: TextView
    private lateinit var summaryAddress: TextView
    private lateinit var summaryDate: TextView
    private lateinit var summaryStartTime: TextView
    private lateinit var summaryEndTime: TextView
    private lateinit var pricePerHour: TextView
    private lateinit var summaryAdditionalRequests: EditText
    private lateinit var summaryTotalPlaygroundPrice: TextView
    private lateinit var summaryTotalEquipmentPrice: TextView
    private lateinit var summaryTotalPrice: TextView

    // fragment buttons
    private lateinit var summaryConfirmButton: Button
    private lateinit var summaryDeleteButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.initConfirmReservationDialog()
        this.initDeleteReservationDialog()

        /* app bar and menu */
        this.initAppBar()
        this.initMenu()

        // clear errors
        viewModel.clearReservationAdditionalRequestsError()

        // setup error observers
        viewModel.getReservationAdditionalRequestError.observe(viewLifecycleOwner) {
            if (it != null) {
                showToasty("error", requireContext(), it.message())
            }
        }

        this.checkAndInitReservationData()

        /* init views */
        this.initViews()

        /* init buttons */
        this.initButtons()
    }

    /* app bar and menu */

    private fun initAppBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        actionBar?.let {
            // show back arrow and the right title
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
            it.title = "Reservation Summary"
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

    /* views */
    private fun initViews() {

        //retrieve views
        summarySportEmoji = requireView().findViewById(R.id.sport_emoji)
        summarySportCenterName =
            requireView().findViewById(R.id.reservation_summary_sport_center_name)
        summaryPlaygroundName = requireView().findViewById(R.id.reservation_summary_playground_name)
        summarySportName = requireView().findViewById(R.id.reservation_summary_sport_name)
        summaryAddress = requireView().findViewById(R.id.reservation_summary_playground_address)
        summaryDate = requireView().findViewById(R.id.reservation_summary_date)
        summaryStartTime = requireView().findViewById(R.id.reservation_summary_start_time)
        summaryEndTime = requireView().findViewById(R.id.reservation_summary_end_time)
        pricePerHour =
            requireView().findViewById(R.id.reservation_summary_playground_price_per_hour)
        summaryAdditionalRequests = requireView().findViewById(R.id.edit_additional_requests)
        summaryTotalPlaygroundPrice =
            requireView().findViewById(R.id.reservation_summary_playground_total_price)
        summaryTotalEquipmentPrice =
            requireView().findViewById(R.id.reservation_summary_equipment_total_price)
        summaryTotalPrice = requireView().findViewById(R.id.reservation_summary_total_price)

        // set text in views
        summarySportEmoji.text = viewModel.reservation.value?.sportEmoji
        summarySportCenterName.text = viewModel.reservation.value?.sportCenterName
        summaryPlaygroundName.text = viewModel.reservation.value?.playgroundName
        summarySportName.text = viewModel.reservation.value?.sportName
        summaryAddress.text = viewModel.reservation.value?.sportCenterAddress

        viewModel.reservation.value?.startTime?.let {
            summaryDate.text = when (it.toLocalDate()) {
                LocalDate.now() -> "Today"
                LocalDate.now().plusDays(1) -> "Tomorrow"
                LocalDate.now().minusDays(1) -> "Yesterday"
                else -> it.format(
                    DateTimeFormatter.ofPattern("EEEE, d MMMM y", Locale.ENGLISH)
                )
            }
        }

        summaryStartTime.text = viewModel.reservation.value?.startTime?.toLocalTime().toString()
        summaryEndTime.text = viewModel.reservation.value?.endTime?.toLocalTime().toString()

        // equipment list
        viewModel.reservation.value?.selectedEquipments?.let {

            val equipmentListContainer =
                requireView().findViewById<LinearLayout>(R.id.reservation_summary_equipment_list_container)

            val equipmentDivider =
                requireView().findViewById<View>(R.id.reservation_summary_equipment_divider)

            if (it.isNotEmpty()) {
                equipmentListContainer.visibility = View.VISIBLE
                equipmentDivider.visibility = View.VISIBLE
                inflateEquipmentList(it, equipmentListContainer)
            } else {
                equipmentListContainer.visibility = View.GONE
                equipmentDivider.visibility = View.GONE
            }
        }

        // prices computation and display
        pricePerHour.text =
            String.format("%.2f", viewModel.reservation.value?.playgroundPricePerHour)

        val duration = Duration.between(
            viewModel.reservation.value?.startTime,
            viewModel.reservation.value?.endTime
        ).toMinutes()

        // setup additional requests text listener
        summaryAdditionalRequests.addTextChangedListener(textListenerInit())

        // setup additional requests observer
        viewModel.reservationAdditionalRequests.observe(viewLifecycleOwner) { result ->
            summaryAdditionalRequests.setText(result)
        }

        val totalPlaygroundPrice =
            duration * viewModel.reservation.value?.playgroundPricePerHour!! / 60

        val totalEquipmentPrice = viewModel.reservation.value?.selectedEquipments?.sumOf {
            it.selectedQuantity * it.unitPrice.toDouble()
        }

        if (totalEquipmentPrice == 0.0) {
            val equipmentPriceLayout =
                requireView().findViewById<LinearLayout>(R.id.reservation_summary_equipment_price_layout)
            equipmentPriceLayout.visibility = View.GONE
        }

        val totalPrice = totalPlaygroundPrice + (totalEquipmentPrice ?: 0.0)

        summaryTotalPlaygroundPrice.text = String.format("%.2f", totalPlaygroundPrice)
        summaryTotalEquipmentPrice.text = String.format("%.2f", totalEquipmentPrice)
        summaryTotalPrice.text = String.format("%.2f", totalPrice)

    }

    private fun textListenerInit(): TextWatcher {

        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val additionalRequests = summaryAdditionalRequests.text.toString()

                if (additionalRequests != "") {
                    viewModel.setReservationAdditionalRequests(additionalRequests)
                } else {
                    viewModel.setReservationAdditionalRequests(null)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        }
    }

    /* equipment list */
    private fun inflateEquipmentList(
        equipmentList: List<NewReservationEquipment>,
        container: LinearLayout
    ) {

        equipmentList.forEach {
            val equipmentView = layoutInflater.inflate(
                R.layout.equipment_list_item_reservation_summary,
                container,
                false
            )

            val equipmentName =
                equipmentView.findViewById<TextView>(R.id.equipment_name)
            val equipmentQuantity =
                equipmentView.findViewById<TextView>(R.id.equipment_quantity)
            val equipmentPrice =
                equipmentView.findViewById<TextView>(R.id.equipment_unit_price)

            equipmentName.text = it.equipmentName
            equipmentQuantity.text = it.selectedQuantity.toString()
            equipmentPrice.text = String.format("%.2f", it.unitPrice.toDouble())

            container.addView(equipmentView)
        }
    }

    /* buttons */
    private fun initButtons() {
        summaryConfirmButton = requireView().findViewById(R.id.reservation_summary_confirm_button)
        summaryDeleteButton = requireView().findViewById(R.id.reservation_summary_delete_button)

        summaryConfirmButton.setOnClickListener {
            confirmReservationDialog.show()
        }

        summaryDeleteButton.setOnClickListener {
            deleteReservationDialog.show()
        }
    }

    /* Confirm reservation dialog */
    private fun initConfirmReservationDialog() {
        confirmReservationDialog = AlertDialog.Builder(requireContext())
            .setMessage("Do you want to confirm this reservation?")
            .setPositiveButton("YES") { _, _ ->
                viewModel.permanentlySaveReservation { saveResult ->
                    when (saveResult) {
                        is Error -> {
                            // an error occurred during reservation update
                            Log.e("unexpected error", "an error occurred during reservation update")

                            showToasty(
                                "error",
                                requireContext(),
                                saveResult.errorMessage(),
                                Toasty.LENGTH_LONG
                            )
                        }

                        is Success -> {
                            val newReservationId = saveResult.value

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
                    }
                }
            }
            .setNegativeButton("NO") { d, _ -> d.cancel() }
            .create()
    }

    /* Cancel reservation dialog */
    private fun initDeleteReservationDialog() {
        deleteReservationDialog = AlertDialog.Builder(requireContext())
            .setMessage("Do you want exit without saving?")
            .setPositiveButton("YES") { _, _ ->

                // navigate back to the home
                findNavController().popBackStack(R.id.showReservationsFragment, false)
            }
            .setNegativeButton("NO") { d, _ -> d.cancel() }
            .create()
    }

    /* reservation */

    private fun checkAndInitReservationData() {
        val reservationBundle = arguments?.getBundle("reservation")

        if (reservationBundle == null) {
            showToasty(
                "error",
                requireContext(),
                "Error: reservation data absent"
            )
            return
        }

        val reservationId = reservationBundle.getString("reservation_id")
        val startSlotStr = reservationBundle.getString("start_slot")
        val endSlotStr = reservationBundle.getString("end_slot")
        val slotDurationMins = reservationBundle.getInt("slot_duration_mins")
        val playgroundId = reservationBundle.getString("playground_id")
        val playgroundName = reservationBundle.getString("playground_name")
        val sportId = reservationBundle.getString("sport_id")
        val sportEmoji = reservationBundle.getString("sport_emoji")
        val sportName = reservationBundle.getString("sport_name")
        val sportCenterId = reservationBundle.getString("sport_center_id")
        val sportCenterName = reservationBundle.getString("sport_center_name")
        val sportCenterAddress = reservationBundle.getString("sport_center_address")
        val equipmentsBundle = reservationBundle.getBundle("equipments")
        val playgroundPricePerHour = reservationBundle.getFloat("playground_price_per_hour")

        val inputErrors = mutableListOf<String>()

        if (startSlotStr == null)
            inputErrors.add("start_slot")

        if (endSlotStr == null)
            inputErrors.add("end_slot")

        if (slotDurationMins == 0)
            inputErrors.add("slot_duration_mins")

        if (playgroundId == null)
            inputErrors.add("playground_id")

        if (playgroundName == null)
            inputErrors.add("playground_name")

        if (sportId == null)
            inputErrors.add("sport_id")

        if (sportEmoji == null)
            inputErrors.add("sport_emoji")

        if (sportName == null)
            inputErrors.add("sport_name")

        if (sportCenterId == null)
            inputErrors.add("sport_center_id")

        if (sportCenterName == null)
            inputErrors.add("sport_center_name")

        if (sportCenterAddress == null)
            inputErrors.add("sport_center_address")

        if (equipmentsBundle == null)
            inputErrors.add("equipments")

        if (playgroundPricePerHour == 0f)
            inputErrors.add("playground_price_per_hour")

        if (inputErrors.isNotEmpty()) {
            // some input fields is/are missing
            showToasty(
                "error",
                requireContext(),
                inputErrors.joinToString(
                    separator = ", ",
                    prefix = "Error: the following input fields are missing: "
                ),
                Toasty.LENGTH_LONG
            )
            return
        }

        // get additional requests from db if any
        if(reservationId != null)
            viewModel.getReservationAdditionalRequest(reservationId)

        // * all data are available here *
        val startTime = LocalDateTime.parse(startSlotStr)
        val endTime =
            LocalDateTime.parse(endSlotStr).plus(Duration.ofMinutes(slotDurationMins.toLong()))
        val selectedEquipments = mutableListOf<NewReservationEquipment>()

        equipmentsBundle!!.keySet().forEach { equipmentId ->
            val equipment = equipmentsBundle.getBundle(equipmentId)!!

            val newEquipmentEquipmentId = equipment.getString("equipment_id")
            val newEquipmentEquipmentName = equipment.getString("equipment_name")
            val newEquipmentSelectedQuantity = equipment.getInt("selected_quantity")
            val newEquipmentUnitPrice = equipment.getFloat("unit_price")

            if (newEquipmentEquipmentId == null || newEquipmentEquipmentName == null ||
                newEquipmentSelectedQuantity == 0 || newEquipmentUnitPrice == 0f
            ) {
                showToasty("error", requireContext(), "Error in fields for an equipment")
                return
            }

            val newEquipment = NewReservationEquipment(
                newEquipmentEquipmentId,
                newEquipmentEquipmentName,
                newEquipmentSelectedQuantity,
                newEquipmentUnitPrice
            )

            selectedEquipments.add(newEquipment)
        }

        // create reservation object
        val newReservation = NewReservation(
            reservationId,
            startTime,
            endTime,
            playgroundId!!,
            playgroundName!!,
            playgroundPricePerHour,
            sportId!!,
            sportEmoji!!,
            sportName!!,
            sportCenterId!!,
            sportCenterName!!,
            sportCenterAddress!!,
            selectedEquipments,
            null
        )

        // save it
        viewModel.setReservation(newReservation)
    }
}