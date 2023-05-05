package it.polito.mad.sportapp.reservation_details

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.showToasty
import it.polito.mad.sportapp.show_reservations.ShowReservationsActivity
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ReservationDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ReservationDetailsFragment()
    }

    private lateinit var viewModel: ReservationDetailsFragmentViewModel

    //views
    private lateinit var qrCode: ImageView
    private lateinit var reservationNumber: TextView
    private lateinit var reservationDate: TextView
    private lateinit var reservationStartTime: TextView
    private lateinit var reservationEndTime: TextView
    private lateinit var reservationSport: TextView
    private lateinit var reservationPlayground: TextView
    private lateinit var reservationSportCenter: TextView
    private lateinit var reservationSportCenterAddress: TextView
    private lateinit var directionsButton: ImageButton
    private lateinit var noEquipmentMessage: TextView
    private lateinit var equipment: LinearLayout
    private lateinit var editButton: ImageButton
    private lateinit var reservationTotalPrice: TextView
    private lateinit var deleteButton: Button

    private var eventId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Generate QR code
        qrCode = requireView().findViewById(R.id.QR_code)
        viewModel.reservation.value?.let { setQRCodeView(it, qrCode) }

        // Retrieve views
        retrieveViews()

        // Initialize values
        initializeValues()
        initializeEquipment()

        // add link to Google Maps
        directionsButton.setOnClickListener {
            handleDirectionsButton(reservationSportCenterAddress.text.toString())
        }

        editButton.setOnClickListener {
            toEditView()
        }

        deleteButton.setOnClickListener {
            startDialog()
        }

        viewModel.reservation.observe(viewLifecycleOwner) {
            if (viewModel.reservation.value != null) {
                setQRCodeView(viewModel.reservation.value!!, qrCode)
                initializeValues()
                initializeEquipment()
            }
        }
        return inflater.inflate(R.layout.fragment_reservation_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[ReservationDetailsFragmentViewModel::class.java]
        // TODO: Use the ViewModel

        //TODO: How to retrieve the id number?
        eventId = 1
        //eventId = intent.getIntExtra("id_event", -1)


    }

    override fun onResume() {
        super.onResume()
        if (eventId != -1)
            viewModel.getReservationFromDb(eventId)
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
        directionsButton = requireView().findViewById(R.id.directionsButton)
        noEquipmentMessage = requireView().findViewById(R.id.noEquipmentMessage)
        reservationTotalPrice = requireView().findViewById(R.id.reservationPrice)
        equipment = requireView().findViewById(R.id.equipmentContainer)
        editButton = requireView().findViewById(R.id.editButton)
        deleteButton = requireView().findViewById(R.id.button_delete_reservation)
    }

    private fun initializeValues() {
        reservationNumber.text =
            "Reservation number: " + String.format("%010d", viewModel.reservation.value?.id)
        reservationDate.text =
            viewModel.reservation.value?.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        reservationStartTime.text =
            viewModel.reservation.value?.startTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        reservationEndTime.text =
            viewModel.reservation.value?.endTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        reservationSport.text = viewModel.reservation.value?.sportName
        reservationPlayground.text = viewModel.reservation.value?.playgroundName
        reservationSportCenter.text = viewModel.reservation.value?.sportCenterName
        reservationSportCenterAddress.text = viewModel.reservation.value?.location
        reservationTotalPrice.text = "€ " + String.format("%.2f", viewModel.reservation.value?.totalPrice)
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
                if (viewModel.deleteReservation()) showToasty(
                    "success",
                    requireContext(),
                    "Reservation correctly deleted"
                )
                val intent = Intent(requireContext(), ShowReservationsActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("NO") { d, _ -> d.cancel() }
            .create()
            .show()
    }

}