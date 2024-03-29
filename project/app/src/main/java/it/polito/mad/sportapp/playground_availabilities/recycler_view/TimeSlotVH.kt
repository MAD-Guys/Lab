package it.polito.mad.sportapp.playground_availabilities.recycler_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.reservation_management.ReservationManagementModeWrapper
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeSlotVH(
    val view: View,
    private val navigateToPlayground: (String, LocalDateTime) -> Unit,
    private val reservationManagementModeWrapper: ReservationManagementModeWrapper,
    private val reservationBundle: Bundle?,
    private val setReservationBundle: (Bundle) -> Unit,
    private val switchToAddMode: () -> Unit
) : AbstractTimeSlotVH(view) {
    private val currentDate = LocalDateTime.now()
    private val startTimeSlotText = view.findViewById<TextView>(R.id.start_time_slot)
    private val endTimeSlotText = view.findViewById<TextView>(R.id.end_time_slot)
    private val availablePlaygroundsContainer =
        view.findViewById<LinearLayout>(R.id.available_playgrounds_container)

    private lateinit var slot: LocalDateTime
    private lateinit var slotDuration: Duration

    init {
        if (reservationManagementModeWrapper.mode != null) {
            val timeSlotBox = view.findViewById<View>(R.id.time_slot_box)
            timeSlotBox.setBackgroundResource(reservationManagementModeWrapper.mode!!.variantColorId)
        }
    }

    fun setTimeSlotTimes(start: LocalDateTime, end: LocalDateTime, slotDuration: Duration) {

        val timeSlotBox = view.findViewById<View>(R.id.time_slot_box)

        slot = start
        this.slotDuration = slotDuration

        startTimeSlotText.text = start.format(DateTimeFormatter.ofPattern("HH:mm"))
        endTimeSlotText.text = end.format(DateTimeFormatter.ofPattern("HH:mm"))

        if (currentDate.isAfter(start)) {
            timeSlotBox.setBackgroundResource(R.color.time_slot_unavailable_background_color)
        } else {
            timeSlotBox.setBackgroundResource(R.color.time_slot_background_color)
        }

        if (reservationManagementModeWrapper.mode != null) {
            timeSlotBox.setBackgroundResource(reservationManagementModeWrapper.mode!!.variantColorId)
        }
    }

    fun setAvailablePlaygrounds(availablePlaygrounds: List<Pair<DetailedPlaygroundSport, PlaygroundAvailabilitiesAdapter.SelectionState>>) {
        availablePlaygroundsContainer.removeAllViews()  // clear playgrounds

        // transform each available playground object in a box view
        val availablePlaygroundBoxes = availablePlaygrounds.map { (playground, selectionState) ->
            // set box as busy, selected, selectable or unselected
            val playgroundSkeletonBox = when {
                !playground.available -> R.layout.unavailable_playground_item
                selectionState == PlaygroundAvailabilitiesAdapter.SelectionState.SELECTED -> R.layout.selected_playground_item
                selectionState == PlaygroundAvailabilitiesAdapter.SelectionState.SELECTABLE -> R.layout.selectable_playground_item
                selectionState == PlaygroundAvailabilitiesAdapter.SelectionState.UNSELECTED -> R.layout.unselected_playground_item
                else -> throw Exception("Unexpected state caught")
            }

            val playgroundBox = LayoutInflater.from(view.context).inflate(
                playgroundSkeletonBox,
                availablePlaygroundsContainer,
                false
            )

            // retrieve available playground' fields views
            val playgroundNameText = playgroundBox.findViewById<TextView>(R.id.playground_name)
            val sportCenterNameText = playgroundBox.findViewById<TextView>(R.id.sport_center_name)
            val pricePerHourText = playgroundBox.findViewById<TextView>(R.id.price_per_hour)

            // set right information
            playgroundNameText.text = playground.playgroundName
            sportCenterNameText.text = playground.sportCenterName
            pricePerHourText.text = String.format("%.2f €/h", playground.pricePerHour)


            // attach listener to navigate to that playground
            playgroundBox.setOnClickListener {
                handlePlaygroundSlotSelection(playground, selectionState)
            }

            playgroundBox.setOnLongClickListener {
                handleSwitchToAddMode(playground)
            }

            playgroundBox
        }

        // push the playgrounds boxes into the container
        availablePlaygroundBoxes.forEach { availablePlaygroundsContainer.addView(it) }
    }

    private fun handlePlaygroundSlotSelection(
        playground: DetailedPlaygroundSport,
        selectionState: PlaygroundAvailabilitiesAdapter.SelectionState
    ) {
        if (!playground.available) return

        if (reservationManagementModeWrapper.mode == null) {
            navigateToPlayground(playground.playgroundId, slot)
            return
        }

        // * add or edit mode *
        reservationBundle?.let { bundle ->
            // (1) this is the very first selected slot (e.g. at the beginning of add mode)
            if (bundle.getString("start_slot") == null) {
                bundle.putString("start_slot", slot.toString())
                bundle.putInt("slot_duration_mins", slotDuration.toMinutes().toInt())
                bundle.putString("playground_id", playground.playgroundId)
                bundle.putString("playground_name", playground.playgroundName)
                bundle.putString("sport_id", playground.sportId)
                bundle.putString("sport_emoji", playground.sportEmoji)
                bundle.putString("sport_name", playground.sportName)
                bundle.putString("sport_center_id", playground.sportCenterId)
                bundle.putString("sport_center_name", playground.sportCenterName)
                bundle.putString("sport_center_address", playground.sportCenterAddress)
                bundle.putFloat("playground_price_per_hour", playground.pricePerHour)
            }
            // (2) one (start) slot is already selected ->
            //      check if to either extend the same selection or restart a new one
            else if (bundle.getString("end_slot") == null) {
                // if user re-click to the same slot (of the same playground), nothing happens
                if (bundle.getString("start_slot") == slot.toString() &&
                    bundle.getString("playground_id") == playground.playgroundId
                )
                    return@let

                if (selectionState == PlaygroundAvailabilitiesAdapter.SelectionState.SELECTABLE) {
                    // extend selection
                    bundle.putString("end_slot", slot.toString())
                } else {
                    // restart selection
                    bundle.putString("start_slot", slot.toString())
                    bundle.remove("end_slot")
                    bundle.putInt("slot_duration_mins", slotDuration.toMinutes().toInt())
                    bundle.putString("playground_id", playground.playgroundId)
                    bundle.putString("playground_name", playground.playgroundName)
                    bundle.putString("sport_id", playground.sportId)
                    bundle.putString("sport_emoji", playground.sportEmoji)
                    bundle.putString("sport_name", playground.sportName)
                    bundle.putString("sport_center_id", playground.sportCenterId)
                    bundle.putString("sport_center_name", playground.sportCenterName)
                    bundle.putString("sport_center_address", playground.sportCenterAddress)
                    bundle.putFloat("playground_price_per_hour", playground.pricePerHour)
                }
            }
            // (3) start and end slots were already selected -> ...
            else {
                // ...restart selection
                bundle.putString("start_slot", slot.toString())
                bundle.remove("end_slot")
                bundle.putInt("slot_duration_mins", slotDuration.toMinutes().toInt())
                bundle.putString("playground_id", playground.playgroundId)
                bundle.putString("playground_name", playground.playgroundName)
                bundle.putString("sport_id", playground.sportId)
                bundle.putString("sport_emoji", playground.sportEmoji)
                bundle.putString("sport_name", playground.sportName)
                bundle.putString("sport_center_id", playground.sportCenterId)
                bundle.putString("sport_center_name", playground.sportCenterName)
                bundle.putString("sport_center_address", playground.sportCenterAddress)
                bundle.putFloat("playground_price_per_hour", playground.pricePerHour)
            }

            // update bundle live data to refresh everything
            setReservationBundle(bundle)
        }
    }

    private fun handleSwitchToAddMode(
        playground: DetailedPlaygroundSport,
    ): Boolean {
        // perform just in show mode (no add/edit mode)
        if (reservationManagementModeWrapper.mode != null) return false

        if (!playground.available) return false

        // create the new reservation bundle containing this slot as (the only) selected one
        val newBundle = bundleOf(
            "start_slot" to slot.toString(),
            "slot_duration_mins" to slotDuration.toMinutes().toInt(),
            "playground_id" to playground.playgroundId,
            "playground_name" to playground.playgroundName,
            "sport_id" to playground.sportId,
            "sport_emoji" to playground.sportEmoji,
            "sport_name" to playground.sportName,
            "sport_center_id" to playground.sportCenterId,
            "sport_center_name" to playground.sportCenterName,
            "sport_center_address" to playground.sportCenterAddress,
            "playground_price_per_hour" to playground.pricePerHour
        )

        // set the bundle and switch to edit mode
        setReservationBundle(newBundle)
        switchToAddMode()

        return true
    }
}