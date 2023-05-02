package it.polito.mad.sportapp.reservation_details

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.EquipmentReservation
import it.polito.mad.sportapp.showToasty

@AndroidEntryPoint
class EditEquipmentActivity : AppCompatActivity() {

    private lateinit var reservationNumberTitle: TextView
    private lateinit var selectedEquipmentContainer: LinearLayout
    private lateinit var availableEquipmentContainer: LinearLayout
    private lateinit var newPrice: TextView
    private lateinit var noEquipmentSelectedMessage: TextView
    private lateinit var noEquipmentAvailableMessage: TextView
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    private var eventId: Int = -1

    private val vm by viewModels<EditEquipmentViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_equipment)

        reservationNumberTitle = findViewById(R.id.reservationNumber)
        selectedEquipmentContainer = findViewById(R.id.selectedEquipmentContainer)
        availableEquipmentContainer = findViewById(R.id.availableEquipmentContainer)
        noEquipmentSelectedMessage = findViewById(R.id.noEquipmentSelectedMessage)
        noEquipmentAvailableMessage = findViewById(R.id.noEquipmentAvailableMessage)
        newPrice = findViewById(R.id.newPrice)
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)

        eventId = intent.getIntExtra("id_event", -1)

        vm.reservation.observe(this) {
            if (vm.reservation.value != null) {
                vm.getEquipmentFromDb(vm.reservation.value!!)
                vm.initSelectedEquipments()
                initViews()
                initButtons()
            }
        }
        vm.tempSelectedEquipment.observe(this) {
            if (vm.reservation.value != null) {
                loadSelectedEquipment(it)
                selectedEquipmentContainer.requestLayout()
            }
        }
        vm.tempAvailableEquipment.observe(this) {
            if (vm.reservation.value != null) {
                vm.initAvailableEquipments()
                loadAvailableEquipment(it)
                availableEquipmentContainer.requestLayout()
            }
        }
        vm.tempPrice.observe(this) { updatedPrice ->
            if (vm.reservation.value != null) {
                newPrice.text = String.format("%.2f", updatedPrice)
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
        inflater.inflate(R.menu.reservation_equipment_menu, menu)
        // change app bar's title
        supportActionBar?.title = "Reservation Equipment"

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

    private fun initViews() {

        reservationNumberTitle.text =
            "Reservation number: " + String.format("%010d", vm.reservation.value?.id)
        newPrice.text = String.format("%.2f", vm.tempPrice.value)

        loadSelectedEquipment(vm.tempSelectedEquipment.value)
        loadAvailableEquipment(vm.tempAvailableEquipment.value)
        initButtons()
    }

    private fun loadAvailableEquipment(availableList: MutableList<Equipment>?) {

        noEquipmentAvailableMessage.visibility = TextView.GONE

        availableEquipmentContainer.removeAllViewsInLayout()

        if (!availableList.isNullOrEmpty()) {
            for ((index, e) in availableList.withIndex()) {

                val row = layoutInflater.inflate(
                    R.layout.available_equipment,
                    availableEquipmentContainer,
                    false
                )

                row.id = index
                val equipmentName = row.findViewById<TextView>(R.id.equipmentName)
                val remainingQuantity = row.findViewById<TextView>(R.id.remainingQty)
                val equipmentPrice = row.findViewById<TextView>(R.id.equipmentPrice)

                equipmentName.text = e.name
                remainingQuantity.text = String.format("%d", e.availability)
                equipmentPrice.text = String.format("%.2f", e.price)

                val button = row.findViewById<ImageButton>(R.id.addEquipmentButton)
                button.setOnClickListener {
                    vm.addEquipment(vm.reservation.value?.id!!, e.id, e.price, e.name)
                }

                availableEquipmentContainer.addView(row)
            }
        } else {
            noEquipmentAvailableMessage.visibility = TextView.VISIBLE
        }
    }

    private fun loadSelectedEquipment(selectedList: MutableList<EquipmentReservation>?) {

        if (selectedList != null) {

            noEquipmentSelectedMessage.visibility = TextView.GONE

            selectedEquipmentContainer.removeAllViewsInLayout()

            for ((index, e) in selectedList.withIndex()) {

                val row = layoutInflater.inflate(
                    R.layout.selected_equipment,
                    selectedEquipmentContainer,
                    false
                )

                row.id = index
                val equipmentName = row.findViewById<TextView>(R.id.equipmentName)
                val equipmentQuantity = row.findViewById<TextView>(R.id.equipmentQuantity)
                val equipmentPrice = row.findViewById<TextView>(R.id.equipmentPrice)

                equipmentName.text = e.equipmentName
                equipmentQuantity.text = String.format("%d", e.quantity)
                equipmentPrice.text = String.format("%.2f", e.totalPrice)

                val buttonPlus = row.findViewById<ImageButton>(R.id.plusButton)
                buttonPlus.setOnClickListener {
                    if (vm.incrementQuantity(e.equipmentId)) {
                        //loadSelectedEquipment(vm.tempSelectedEquipment.value)
                    } else {
                        showToasty(
                            "info",
                            this,
                            "Every available ${e.equipmentName} is booked!"
                        )
                    }
                }

                val buttonMinus = row.findViewById<ImageButton>(R.id.minusButton)
                buttonMinus.setOnClickListener {
                    vm.decrementQuantity(e.equipmentId)
                }

                selectedEquipmentContainer.addView(row, index)
            }
        } else {
            noEquipmentSelectedMessage.visibility = TextView.VISIBLE
        }
    }

    private fun initButtons() {
        cancelButton.setOnClickListener {
            this.finish()
        }

        saveButton.setOnClickListener {
            vm.saveEquipment()
            showToasty("success", this, "Information correctly saved!")
        }
    }
}