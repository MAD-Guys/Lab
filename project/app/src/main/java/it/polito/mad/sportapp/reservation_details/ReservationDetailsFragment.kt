package it.polito.mad.sportapp.reservation_details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.showToasty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class ReservationDetailsFragment : Fragment(R.layout.fragment_reservation_details) {
    private lateinit var qrCode: ImageView
    private lateinit var reservationNumber: TextView
    private lateinit var reservationDate: TextView
    private lateinit var reservationStartTime: TextView
    private lateinit var reservationEndTime: TextView
    private lateinit var reservationSport: TextView
    private lateinit var reservationPlayground: TextView
    private lateinit var reservationSportCenter: TextView
    private lateinit var reservationSportCenterAddress: TextView
    private lateinit var playgroundButton: Button
    private lateinit var noEquipmentMessage: TextView
    private lateinit var equipment: LinearLayout
    private lateinit var editButton: ImageButton
    private lateinit var reservationTotalPrice: TextView
    private lateinit var deleteButton: Button

    private lateinit var navController: NavController
    private lateinit var bottomNavigationBar: View

    // action bar
    private var actionBar: ActionBar? = null

    private val viewModel by viewModels<ReservationDetailsViewModel>()

    private var eventId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // get bottom navigation bar
        bottomNavigationBar = requireActivity().findViewById(R.id.bottom_navigation_bar)

        // hide bottom navigation bar
        bottomNavigationBar.visibility = View.GONE

        // initialize menu
        menuInit()

        // initialize navigation controller
        navController = Navigation.findNavController(view)

        // Retrieve event id
        eventId = arguments?.getInt("id_event") ?: -1

        // Generate QR code
        qrCode = requireView().findViewById(R.id.QR_code)
        viewModel.reservation.value?.let { setQRCodeView(it, qrCode) }

        // Retrieve views
        retrieveViews()

        // Initialize values
        // initializeValues()
        initializeEquipment()

        // add link to Playground Details
        playgroundButton.setOnClickListener {
            handlePlaygroundButton(1 /*viewModel.reservation.value.playgroundId*/)
        }

        editButton.setOnClickListener {
            toEditView()
        }

        deleteButton.setOnClickListener {
            startDialog()
        }

        viewModel.reservation.observe(viewLifecycleOwner) {
            val reservation = viewModel.reservation.value

            if (reservation != null) {
                setQRCodeView(reservation, qrCode)
                initializeValues()
                initializeEquipment()

                // show delete reservation button only if the reservation starts in the future
                val currentDateTime = LocalDateTime.now()

                if (currentDateTime.isBefore(reservation.startLocalDateTime)) {
                    deleteButton.visibility = Button.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (eventId != -1)
            viewModel.getReservationFromDb(eventId)
    }

    // manage menu item selection
    private fun menuInit() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.reservations_details_menu, menu)

                actionBar?.let {
                    it.setDisplayHomeAsUpEnabled(true)
                    it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
                    it.title = "Reservation Details"
                }

                menu.findItem(R.id.reservation_details_edit_button).isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // handle the menu selection
                return when (menuItem.itemId) {
                    R.id.reservation_details_back_button -> {
                        navController.popBackStack()
                        true
                    }
                    R.id.reservation_details_edit_button -> {
                        //TODO: Navigate to edit
                        showToasty("info", requireContext(), "Edit reservation")
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun retrieveViews() {
        reservationNumber = requireView().findViewById(R.id.reservationNumber)
        reservationDate = requireView().findViewById(R.id.reservationDate)
        reservationStartTime = requireView().findViewById(R.id.reservationStartTime)
        reservationEndTime = requireView().findViewById(R.id.reservationEndTime)
        reservationSport = requireView().findViewById(R.id.reservationSport)
        reservationPlayground = requireView().findViewById(R.id.reservationPlaygroundName)
        reservationSportCenter = requireView().findViewById(R.id.reservationSportCenter)
        reservationSportCenterAddress = requireView().findViewById(R.id.reservationAddress)
        playgroundButton = requireView().findViewById(R.id.button_playground_details)
        noEquipmentMessage = requireView().findViewById(R.id.noEquipmentMessage)
        reservationTotalPrice = requireView().findViewById(R.id.reservationPrice)
        equipment = requireView().findViewById(R.id.equipmentContainer)
        editButton = requireView().findViewById(R.id.editButton)
        deleteButton = requireView().findViewById(R.id.button_delete_reservation)
    }

    @SuppressLint("SetTextI18n")
    private fun initializeValues() {
        reservationNumber.text =
            "Reservation number: " + String.format("%010d", viewModel.reservation.value?.id)
        reservationDate.text =
            viewModel.reservation.value?.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        reservationStartTime.text =
            viewModel.reservation.value?.startTime?.format(
                DateTimeFormatter.ofLocalizedTime(
                    FormatStyle.SHORT
                )
            )
        reservationEndTime.text =
            viewModel.reservation.value?.endTime?.format(
                DateTimeFormatter.ofLocalizedTime(
                    FormatStyle.SHORT
                )
            )
        reservationSport.text = viewModel.reservation.value?.sportName
        reservationPlayground.text = viewModel.reservation.value?.playgroundName
        reservationSportCenter.text = viewModel.reservation.value?.sportCenterName
        reservationSportCenterAddress.text = viewModel.reservation.value?.location
        reservationTotalPrice.text =
            "€ " + String.format("%.2f", viewModel.reservation.value?.totalPrice)
    }

    private fun initializeEquipment() {
        equipment.removeAllViewsInLayout()

        if (viewModel.reservation.value?.equipments != null && viewModel.reservation.value?.equipments!!.isNotEmpty()) {

            noEquipmentMessage.visibility = TextView.GONE

            for ((index, e) in viewModel.reservation.value?.equipments!!.withIndex()) {
                val row = layoutInflater.inflate(R.layout.equipment_row, equipment, false)
                row.id = index
                val equipmentName = row.findViewById<TextView>(R.id.equipmentName)
                val equipmentQuantity = row.findViewById<TextView>(R.id.equipmentQuantity)
                val equipmentPrice = row.findViewById<TextView>(R.id.equipmentPrice)

                equipmentName.text = e.equipmentName
                equipmentQuantity.text = String.format("%d", e.quantity)
                equipmentPrice.text = String.format("%.2f", e.totalPrice)

                equipment.addView(row, index)
            }
        } else {
            noEquipmentMessage.visibility = TextView.VISIBLE
        }
    }

    private fun handleDirectionsButton(address: String) {

        val formattedAddress = address.replace(" ", "+")
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$formattedAddress")
        )
        startActivity(intent)
    }

    private fun handlePlaygroundButton(playgroundId : Int){
        val bundle = bundleOf("id_playground" to playgroundId)
        navController.navigate(R.id.action_reservationDetailsFragment_to_PlaygroundDetailsFragment, bundle)
    }

    private fun toEditView() {
        //TODO: USE NAVIGATION
        val intent = Intent(requireContext(), EditEquipmentActivity::class.java)
        intent.putExtra("id_event", eventId)
        startActivity(intent)
    }

    private fun startDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure to delete this reservation?")
            .setPositiveButton("YES") { _, _ ->
                if (viewModel.deleteReservation()) {
                    // find and navigate to the previous (caller) fragment
                    navController.popBackStack()

                    showToasty(
                        "success",
                        requireContext(),
                        "Reservation correctly deleted"
                    )
                } else {
                    showToasty(
                        "error",
                        requireContext(),
                        "An unexpected error occurred during delete reservation"
                    )
                }
            }
            .setNegativeButton("NO") { d, _ -> d.cancel() }
            .create()
            .show()
    }

}