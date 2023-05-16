package it.polito.mad.sportapp.reservation_management.equipments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import es.dmoral.toasty.Toasty
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.showToasty

internal fun ManageEquipmentsFragment.initAppBar() {
    val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

    actionBar?.let {
        // show back arrow and the right title
        it.setDisplayHomeAsUpEnabled(true)
        it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        it.title = "Reservation equipments"
    }
}

internal fun ManageEquipmentsFragment.initMenu() {
    requireActivity().addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(
                R.menu.manage_equipments_menu,
                menu
            )
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.save_equipments_button -> {
                    navigateToReservationSummary()
                    true
                }
                else -> false
            }
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

internal fun ManageEquipmentsFragment.initFloatingButton() {
    floatingButton = requireView().findViewById(R.id.floatingButton)
    floatingButton.setOnClickListener {
        navigateToReservationSummary()
    }
}

private fun ManageEquipmentsFragment.navigateToReservationSummary() {
    // create a bundle containing all the selected equipments and the relative qty
    val params = bundleOf(
        "reservation" to viewModel.reservationBundle.also {
            it.putBundle("equipments", createEquipmentsBundleFrom(viewModel.selectedEquipments.value!!))
        }
    )

    // navigate to reservation summary view
    findNavController().navigate(R.id.action_manageEquipmentsFragment_to_reservationSummaryFragment, params)
}

private fun createEquipmentsBundleFrom(selectedEquipments: Map<Int, DetailedEquipmentReservation>): Bundle {
    val equipmentsBundle = Bundle()

    selectedEquipments.map { (equipmentId, selectedEquipment) ->
        Pair(equipmentId.toString(), bundleOf(
            "equipment_id" to selectedEquipment.equipmentId,
            "equipment_name" to selectedEquipment.equipmentName,
            "selected_quantity" to selectedEquipment.selectedQuantity,
            "unit_price" to selectedEquipment.unitPrice
        ))
    }.forEach { (equipmentId, equipmentBundle) ->
        equipmentsBundle.putBundle(equipmentId, equipmentBundle)
    }

    return equipmentsBundle
}

internal fun ManageEquipmentsFragment.initReservationBundle() {
    /* retrieve reservation data from previous view */
    val reservationBundle = arguments?.getBundle("reservation")

    if (reservationBundle == null) {
        showToasty(
            "error",
            requireContext(),
            "Error: reservation data not found",
            Toasty.LENGTH_LONG
        )
    }
    else {
        viewModel.reservationBundle = Bundle(reservationBundle)
    }
}

@SuppressLint("SetTextI18n")
internal fun ManageEquipmentsFragment.initEquipmentsObservers() {
    // available equipments observer
    viewModel.availableEquipments.observe(this) { newAvailableEquipments ->
        viewModel.selectedEquipments.value?.let { selectedEquipments ->
            val changedEquipments = mutableListOf<String>()

            for ((equipmentId, selectedEquipment) in selectedEquipments) {
                // check that the current selected quantity is still below (or equal) to the max available one
                val maxAvailableQty = newAvailableEquipments[equipmentId]?.availability ?: 0

                if (selectedEquipment.selectedQuantity > maxAvailableQty) {
                    // set the selected quantity to the max possible one
                    selectedEquipments[selectedEquipment.equipmentId]!!.selectedQuantity = maxAvailableQty

                    // register the change
                    changedEquipments.add(selectedEquipment.equipmentName)
                }
            }

            if (changedEquipments.isNotEmpty()) {
                // * some quantities changed *

                // update view model data
                viewModel.setSelectedEquipments(selectedEquipments)

                // inform the user
                showToasty(
                    "warning",
                    requireContext(),
                    changedEquipments.joinToString(", ",
                        prefix="The quantities of the following equipments changed due to modified availability: ")
                )
            }

            // check border lines
            for ((equipmentId, selectedEquipment) in selectedEquipments) {
                // check that the current selected quantity is still below (or equal) to the max available one
                val maxAvailableQty = newAvailableEquipments[equipmentId]?.availability ?: 0

                val incrementQtyButton = equipmentsRowsById[selectedEquipment.equipmentId]?.findViewById<Button>(R.id.increment_equipment_button)
                val decrementQtyButton = equipmentsRowsById[selectedEquipment.equipmentId]?.findViewById<Button>(R.id.decrement_equipment_button)

                if (selectedEquipment.selectedQuantity == maxAvailableQty) {
                    incrementQtyButton?.backgroundTintList = requireContext().getColorStateList(R.color.grey_selector)
                }
                else {
                    // restore orange color
                    incrementQtyButton?.backgroundTintList = requireContext().getColorStateList(R.color.orange_selector)
                }

                if (selectedEquipment.selectedQuantity == 1) {
                    decrementQtyButton?.backgroundTintList = requireContext().getColorStateList(R.color.red_selector)
                }
                else {
                    // restore orange color
                    decrementQtyButton?.backgroundTintList = requireContext().getColorStateList(R.color.orange_selector)
                }
            }

            // check if there is at least one available equipment left
            this.checkAtLeastOneAvailableEquipment(newAvailableEquipments, selectedEquipments)
        }
    }

    // selected equipments observer
    viewModel.selectedEquipments.observe(this) { newSelectedEquipments ->
        val availableEquipments = viewModel.availableEquipments.value
        val equipmentsToRemove = mutableListOf<Int>()

        for ((_, selectedEquipment) in newSelectedEquipments) {
            val selectedQty = selectedEquipment.selectedQuantity
            val previousEquipmentRow = equipmentsRowsById[selectedEquipment.equipmentId]

            if(previousEquipmentRow == null) {
                // * new selected equipment *

                // create equipment view
                val newEquipmentRow = layoutInflater.inflate(
                    R.layout.manage_equipment_row,
                    equipmentsContainer,
                    false
                ) as LinearLayout

                // set up its info
                val equipmentNameText = newEquipmentRow.findViewById<TextView>(R.id.equipment_name)
                val equipmentUnitPriceText = newEquipmentRow.findViewById<TextView>(R.id.equipment_unit_price)
                val equipmentQuantityText = newEquipmentRow.findViewById<TextView>(R.id.equipment_quantity)

                equipmentNameText.text = selectedEquipment.equipmentName
                equipmentUnitPriceText.text = "€${String.format("%.2f", selectedEquipment.unitPrice)}"
                equipmentQuantityText.text = selectedQty.toString()

                // attach equipment quantities callbacks
                val incrementQtyButton = newEquipmentRow.findViewById<Button>(R.id.increment_equipment_button)
                val decrementQtyButton = newEquipmentRow.findViewById<Button>(R.id.decrement_equipment_button)

                // *increment* qty by 1
                incrementQtyButton.setOnClickListener {
                    synchronized(viewModel.selectedEquipments) {
                        val currentSelectedQuantity = viewModel.selectedEquipments.value!![selectedEquipment.equipmentId]?.selectedQuantity ?: 0
                        val maxQuantity = viewModel.availableEquipments.value!![selectedEquipment.equipmentId]?.availability ?: 0

                        if(currentSelectedQuantity >= maxQuantity)  // max qty reached -> no action
                            return@setOnClickListener

                        // *increment* qty and update view model data
                        viewModel.setSelectedEquipments(viewModel.selectedEquipments.value!!.also {
                            it[selectedEquipment.equipmentId]!!.selectedQuantity = currentSelectedQuantity + 1
                        })
                    }
                }

                // *decrement* qty by 1
                decrementQtyButton.setOnClickListener {
                    synchronized(viewModel.selectedEquipments) {
                        val currentSelectedQuantity = viewModel.selectedEquipments.value!![selectedEquipment.equipmentId]?.selectedQuantity ?: 0

                        if (currentSelectedQuantity <= 0)
                            return@setOnClickListener

                        // *decrement* qty and update view model data
                        viewModel.setSelectedEquipments(viewModel.selectedEquipments.value!!.also {
                            it[selectedEquipment.equipmentId]!!.selectedQuantity = currentSelectedQuantity - 1
                        })
                    }
                }

                // attach equipment to the view
                equipmentsContainer.addView(newEquipmentRow)

                // save reference
                equipmentsRowsById[selectedEquipment.equipmentId] = newEquipmentRow
                equipmentsRows.add(newEquipmentRow)

                if(selectedQty == 1) {
                    decrementQtyButton.backgroundTintList = requireContext().getColorStateList(R.color.red_selector)
                }
            }
            else {
                // * already existing equipment *

                if (selectedQty <= 0) {
                    // remove the equipment row
                    val equipmentIndex = equipmentsRows.indexOf(previousEquipmentRow)
                    Log.d("index of item to remove", equipmentIndex.toString())
                    equipmentsContainer.removeViewAt(equipmentIndex)

                    // delete reference
                    equipmentsRowsById.remove(selectedEquipment.equipmentId)
                    equipmentsRows.removeAt(equipmentIndex)

                    // delete entry with qty 0
                    equipmentsToRemove.add(selectedEquipment.equipmentId)
                }
                else {
                    // change equipment quantity shown
                    val equipmentQuantityText = previousEquipmentRow.findViewById<TextView>(R.id.equipment_quantity)
                    equipmentQuantityText.text = selectedQty.toString()

                    if (availableEquipments == null)  // first set
                        return@observe

                    // retrieve quantities buttons
                    val incrementQtyButton = previousEquipmentRow.findViewById<Button>(R.id.increment_equipment_button)
                    val decrementQtyButton = previousEquipmentRow.findViewById<Button>(R.id.decrement_equipment_button)

                    // check if max quantity reached
                    val equipmentMaxQuantity = availableEquipments[selectedEquipment.equipmentId]!!.availability

                    if (selectedQty >= equipmentMaxQuantity) {
                        // set lighter grey color to the '+' button
                        incrementQtyButton.backgroundTintList = requireContext().getColorStateList(R.color.grey_selector)
                    }
                    else {
                        // restore orange color
                        incrementQtyButton.backgroundTintList = requireContext().getColorStateList(R.color.orange_selector)
                    }

                    if (selectedQty <= 1) {
                        // set red color to the '-' button
                        decrementQtyButton.backgroundTintList = requireContext().getColorStateList(R.color.red_selector)
                    }
                    else {
                        // restore orange color
                        decrementQtyButton.backgroundTintList = requireContext().getColorStateList(R.color.orange_selector)
                    }
                }
            }
        }

        // remove equipments with qty 0
        equipmentsToRemove.forEach{ newSelectedEquipments.remove(it) }

        if (availableEquipments == null)  // first set
            return@observe

        // check if any available equipment exist
        this.checkAtLeastOneAvailableEquipment(availableEquipments, newSelectedEquipments)
    }
}

internal fun ManageEquipmentsFragment.checkAtLeastOneAvailableEquipment(
    availableEquipments: Map<Int,Equipment>,
    newSelectedEquipments: Map<Int, DetailedEquipmentReservation>
) {
    var atLeastOneAvailableEquipmentFlag = false

    // check if there is at least one available equipment left
    for ((equipmentId, equipment) in availableEquipments) {
        val actualSelectedEquipments =
            newSelectedEquipments[equipmentId]?.selectedQuantity ?: 0
        val availableQty = equipment.availability - actualSelectedEquipments

        if (availableQty > 0)
            atLeastOneAvailableEquipmentFlag = true
    }

    if (atLeastOneAvailableEquipmentFlag) {
        // there is at least one available equipment
        addEquipmentButtonMessage.text = requireActivity().resources.getText(R.string.add_new_equipment_message)
        addEquipmentButton.backgroundTintList = requireContext().getColorStateList(R.color.blue_selector)
    }
    else {
        addEquipmentButtonMessage.text = requireActivity().resources.getText(R.string.no_more_available_equipments_message)
        addEquipmentButton.backgroundTintList = requireContext().getColorStateList(R.color.grey_selector)
    }
}

internal fun ManageEquipmentsFragment.initAddEquipmentButton() {
    addEquipmentButtonMessage = requireView().findViewById(R.id.add_equipment_button_message)
    addEquipmentButton = requireView().findViewById(R.id.add_equipment_button)

    registerForContextMenu(addEquipmentButton)

    // remove long click listener
    addEquipmentButton.setOnLongClickListener(null)

    // add on click listener to open the context menu and show available equipments
    addEquipmentButton.setOnClickListener {
        requireActivity().openContextMenu(addEquipmentButton)
    }
    addEquipmentButtonMessage.setOnClickListener {
        requireActivity().openContextMenu(addEquipmentButton)
    }
}