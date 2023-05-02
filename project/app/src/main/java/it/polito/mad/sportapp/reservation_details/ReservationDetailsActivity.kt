package it.polito.mad.sportapp.reservation_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.showToasty
import it.polito.mad.sportapp.show_reservations.ShowReservationsActivity
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class ReservationDetailsActivity : AppCompatActivity() {

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

    // view model
    private val vm by viewModels<ReservationDetailsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_details)

        eventId = intent.getIntExtra("id_event", -1)

        // Generate QR code
        qrCode = findViewById(R.id.QR_code)
        vm.reservation.value?.let { setQRCodeView(it, qrCode) }

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

        vm.reservation.observe(this) {
            if (vm.reservation.value != null) {
                setQRCodeView(vm.reservation.value!!, qrCode)
                initializeValues()
                initializeEquipment()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (eventId != -1)
            vm.getReservationFromDb(eventId)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate and render the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.reservations_details_menu, menu)
        // change app bar's title
        supportActionBar?.title = "Reservation Details"

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        // detect which item has been selected and perform corresponding action
        R.id.equipment_back_button -> {
            this.finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun retrieveViews() {
        reservationNumber = findViewById(R.id.reservationNumber)
        reservationDate = findViewById(R.id.reservationDate)
        reservationStartTime = findViewById(R.id.reservationStartTime)
        reservationEndTime = findViewById(R.id.reservationEndTime)
        reservationSport = findViewById(R.id.reservationSport)
        reservationPlayground = findViewById(R.id.reservationPlaygroundName)
        reservationSportCenter = findViewById(R.id.reservationSportCenter)
        reservationSportCenterAddress = findViewById(R.id.reservationAddress)
        directionsButton = findViewById(R.id.directionsButton)
        noEquipmentMessage = findViewById(R.id.noEquipmentMessage)
        reservationTotalPrice = findViewById(R.id.reservationPrice)
        equipment = findViewById(R.id.equipmentContainer)
        editButton = findViewById(R.id.editButton)
        deleteButton = findViewById(R.id.button_delete_reservation)
    }

    private fun initializeValues() {
        reservationNumber.text =
            "Reservation number: " + String.format("%010d", vm.reservation.value?.id)
        reservationDate.text =
            vm.reservation.value?.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        reservationStartTime.text =
            vm.reservation.value?.startTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        reservationEndTime.text =
            vm.reservation.value?.endTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        reservationSport.text = vm.reservation.value?.sportName
        reservationPlayground.text = vm.reservation.value?.playgroundName
        reservationSportCenter.text = vm.reservation.value?.sportCenterName
        reservationSportCenterAddress.text = vm.reservation.value?.location
        reservationTotalPrice.text = "€ " + String.format("%.2f", vm.reservation.value?.totalPrice)
    }

    private fun initializeEquipment() {
        equipment.removeAllViewsInLayout()

        if (vm.reservation.value?.equipments != null && vm.reservation.value?.equipments!!.isNotEmpty()) {

            noEquipmentMessage.visibility = TextView.GONE

            for ((index, e) in vm.reservation.value?.equipments!!.withIndex()) {
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

    private fun toEditView() {
        val intent = Intent(this, EditEquipmentActivity::class.java)
        intent.putExtra("id_event", eventId)
        startActivity(intent)
    }

    private fun handleDirectionsButton(address: String) {

        val formattedAddress = address.replace(" ", "+")
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$formattedAddress")
        )
        startActivity(intent)
    }

    private fun startDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure to delete this reservation?")
            .setPositiveButton("YES") { _, _ ->
                if (vm.deleteReservation()) showToasty(
                    "success",
                    this,
                    "Reservation correctly deleted"
                )
                val intent = Intent(this, ShowReservationsActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("NO") { d, _ -> d.cancel() }
            .create()
            .show()
    }
}