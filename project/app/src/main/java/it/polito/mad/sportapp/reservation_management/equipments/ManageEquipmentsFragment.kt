package it.polito.mad.sportapp.reservation_management.equipments

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import it.polito.mad.sportapp.entities.Equipment

@AndroidEntryPoint
class ManageEquipmentsFragment : Fragment(R.layout.manage_equipments_view) {
    // view model
    internal val viewModel by viewModels<ManageEquipmentsViewModel>()

    internal lateinit var equipmentsContainer: LinearLayout

    // contains all the equipments views, associated to the corresponding equipment id
    internal val equipmentsRowsById = mutableMapOf<String, LinearLayout>()
    internal val equipmentsRows = mutableListOf<LinearLayout>()     // ordered as appearing in the view

    internal lateinit var addEquipmentButtonMessage: TextView
    internal lateinit var addEquipmentButton: Button

    // floating action button
    internal lateinit var floatingButton: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* app bar and menu */
        this.initAppBar()
        this.initMenu()
        this.initFloatingButton()

        /* init error message toast */
        this.initErrorMessageObserver()

        /* retrieve reservation data passed after slots selection */
        this.initReservationBundle()

        /* equipments container */
        equipmentsContainer = view.findViewById(R.id.equipments_container)
        equipmentsContainer.removeAllViews()    // remove default equipments

        /* equipments */
        viewModel.loadEquipmentsQuantitiesAsync()
        this.initEquipmentsObservers()

        /* add equipment button */
        this.initAddEquipmentButton()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        when (v.id) {
            R.id.add_equipment_button -> {
                val selectedEquipments = viewModel.selectedEquipments.value!!

                // compute available equipments' quantities to show
                val availableEquipmentsOptions = mutableMapOf<String, Equipment>()

                for ((equipmentId, equipment) in viewModel.availableEquipments.value!!) {
                    val actualSelectedQuantity = selectedEquipments[equipmentId]?.selectedQuantity ?: 0
                    val actualQuantityLeft = equipment.availability - actualSelectedQuantity

                    if (actualQuantityLeft > 0) {
                        // add this option to the menu
                        availableEquipmentsOptions[equipmentId] =
                            equipment.clone().apply { availability = actualQuantityLeft }
                    }
                }

                // set menu title
                menu.setHeaderTitle("Add a new equipment to your reservation")

                // show menu options
                availableEquipmentsOptions.forEach{(id, equipment) ->
                    val optionText = "+   ${equipment.name} €${String.format("%.2f", equipment.unitPrice)}  (${equipment.availability} left)"
                    val optionId = Menu.FIRST + viewModel.availableEquipmentsIndexesById?.get(id)!!
                    menu.add(ContextMenu.NONE, optionId, ContextMenu.NONE, optionText)
                }

                super.onCreateContextMenu(menu, v, menuInfo)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val selectedEquipmentIdIndex = item.itemId - Menu.FIRST
        val selectedEquipmentId = viewModel.availableEquipmentsIdsByIndexes?.get(selectedEquipmentIdIndex)!!
        Log.d("tapped item", item.title.toString())

        synchronized(viewModel.selectedEquipments) {
            when {
                viewModel.availableEquipments.value!!.contains(selectedEquipmentId) -> {
                    val equipment = viewModel.availableEquipments.value!![selectedEquipmentId]!!
                    val selectedEquipment = viewModel.selectedEquipments.value!![selectedEquipmentId]
                    val newQty = (selectedEquipment?.selectedQuantity ?: 0) + 1

                    // add new equipment with qty 1
                    val newEquipmentReservation = DetailedEquipmentReservation(
                        viewModel.reservationBundle.getString("reservation_id"),
                        selectedEquipmentId,
                        equipment.name,
                        newQty,
                        equipment.unitPrice,
                        equipment.unitPrice * newQty
                    )

                    // save new equipment
                    viewModel.selectedEquipments.value!![selectedEquipmentId] = newEquipmentReservation

                    // update viewModel data
                    viewModel.setSelectedEquipments(viewModel.selectedEquipments.value!!)

                    return true
                }
                else -> return super.onContextItemSelected(item)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // clear equipments rows
        this.equipmentsRows.clear()
        this.equipmentsRowsById.clear()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.fireListener.unregister()
    }
}