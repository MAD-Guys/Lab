package it.polito.mad.sportapp.playground_availabilities.recycler_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.reservation_management.ReservationManagementMode
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.Exception

class PlaygroundAvailabilitiesAdapter(
    // map containing all the slots and their corresponding available playgrounds
    playgroundAvailabilities: Map<LocalDateTime, List<DetailedPlaygroundSport>>,
    internal var selectedDate: LocalDate,
    private val slotDuration: Duration,
    private val reservationManagementMode: ReservationManagementMode?,
    private var originalReservationBundle: Bundle?,
    internal var reservationBundle: Bundle?,
    private val setReservationBundle: (Bundle) -> Unit,
    private val navigateToPlayground: (Int) -> Unit
) : RecyclerView.Adapter<AbstractTimeSlotVH>()
{
    // list of all the interesting slots, chronologically ordered
    private var timeSlots = playgroundAvailabilities.keys.toList().sorted()

    internal var playgroundAvailabilities = playgroundAvailabilities
        set(value) {
            field = value
            timeSlots = value.keys.toList().sorted()
            playgroundAvailabilitiesSelections = this.computeSelectionsOf(value)
        }

    enum class SelectionState {
        SELECTED, SELECTABLE, UNSELECTED
    }

    // it contains the playgrounds associated to their state (selected, unselected or selectable)
    private var playgroundAvailabilitiesSelections = computeSelectionsOf(playgroundAvailabilities)

    override fun getItemCount(): Int {
        return if (timeSlots.isNotEmpty())
            timeSlots.size
        else 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (playgroundAvailabilities.isEmpty())
            // if there are no available playgrounds for the selected date, show a proper alert
            R.layout.no_available_playgrounds_box
        else
            // otherwise show the playground availabilities
            R.layout.time_slot_availabilities_container
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractTimeSlotVH {
        val timeSlotView = LayoutInflater.from(parent.context).inflate(
            viewType, parent, false)

        return when(viewType) {
            R.layout.time_slot_availabilities_container -> TimeSlotVH(
                timeSlotView, navigateToPlayground, reservationManagementMode,
                reservationBundle, setReservationBundle
            )
            R.layout.no_available_playgrounds_box -> NoAvailablePlaygroundsVH(timeSlotView)
            else -> throw Exception("Unexpected view found in onCreateViewHolder")
        }
    }

    override fun onBindViewHolder(holder: AbstractTimeSlotVH, position: Int) {
        if(holder is TimeSlotVH) {
            val timeSlot = timeSlots[position]
            val availablePlaygroundsSelections = playgroundAvailabilitiesSelections[timeSlot]!!

            holder.setTimeSlotTimes(timeSlot, timeSlot.plus(slotDuration), slotDuration)
            holder.setAvailablePlaygrounds(availablePlaygroundsSelections)
        }
    }

    fun smartUpdatePlaygroundAvailabilities(
        newPlaygroundAvailabilities: Map<LocalDateTime, List<DetailedPlaygroundSport>>
    ) {
        val newPlaygroundAvailabilitiesSelections = this.computeSelectionsOf(newPlaygroundAvailabilities)

        // computing differences between previous and new playground availabilities (for the specified date)
        val diffs = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() =
                if (playgroundAvailabilities.isEmpty()) 1
                else playgroundAvailabilities.size

            override fun getNewListSize() =
                if (newPlaygroundAvailabilities.isEmpty()) 1
                else newPlaygroundAvailabilities.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                // from <no availabilities> to <no availabilities>
                if (playgroundAvailabilities.isEmpty() && newPlaygroundAvailabilities.isEmpty())
                    return true

                // from <no availabilities> to <some availabilities> or
                // from <some availabilities> to <no availabilities>
                if ((playgroundAvailabilities.isEmpty() && newPlaygroundAvailabilities.isNotEmpty()) ||
                    (playgroundAvailabilities.isNotEmpty() && newPlaygroundAvailabilities.isEmpty()))
                    return false

                // from <some availabilities> to <some other availabilities>
                val newTimeSlots = newPlaygroundAvailabilities.keys.toList().sorted()

                return timeSlots[oldItemPosition] == newTimeSlots[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                // from <no availabilities> to <no availabilities>
                if (playgroundAvailabilities.isEmpty() && newPlaygroundAvailabilities.isEmpty())
                    return true

                // from <no availabilities> to <some availabilities> or
                // from <some availabilities> to <no availabilities>
                if ((playgroundAvailabilities.isEmpty() && newPlaygroundAvailabilities.isNotEmpty()) ||
                    (playgroundAvailabilities.isNotEmpty() && newPlaygroundAvailabilities.isEmpty()))
                    return false

                // from <some availabilities> to <some other availabilities>
                val oldTimeSlots = timeSlots
                val oldTimeSlot = oldTimeSlots[oldItemPosition]
                val oldPlaygroundsSelections = playgroundAvailabilitiesSelections[oldTimeSlot]!!

                val newTimeSlots = newPlaygroundAvailabilities.keys.toList().sorted()
                val newTimeSlot = newTimeSlots[newItemPosition]
                val newPlaygroundsSelections = newPlaygroundAvailabilitiesSelections[newTimeSlot]!!

                // check exact equality between the two lists
                return oldPlaygroundsSelections.all{ (oldPlayground, selectionState_oldP) ->
                            newPlaygroundsSelections.any{ (newPlayground, selectionState_newP) ->
                                selectionState_oldP == selectionState_newP &&
                                        oldPlayground.exactlyEqualTo(newPlayground)
                            } &&
                       newPlaygroundsSelections.all { (newPlayground, selectionState_newP) ->
                            oldPlaygroundsSelections.any { (oldPlayground, selectionState_oldP) ->
                                selectionState_newP == selectionState_oldP &&
                                    newPlayground.exactlyEqualTo(oldPlayground)
                            }
                    }
                }
            }

        })

        // update playground availabilities
        this.playgroundAvailabilities = newPlaygroundAvailabilities

        // perform smart updates
        diffs.dispatchUpdatesTo(this)
    }

    private fun computeSelectionsOf(
        playgroundAvailabilities: Map<LocalDateTime, List<DetailedPlaygroundSport>>
    ): Map<LocalDateTime, List<Pair<DetailedPlaygroundSport, SelectionState>>>
    {
        val selectedPlaygroundId = reservationBundle?.getInt("playground_id")
        val selectedStartSlotStr = reservationBundle?.getString("start_slot")
        val selectedEndSlotStr = reservationBundle?.getString("end_slot")

        // compute selection state of each playground in each slot
        val playgroundAvailabilitiesSelections = playgroundAvailabilities.mapValues { (slot, availablePlaygrounds) ->
            availablePlaygrounds.map { playground ->
                // slot for different playgrounds rather than the selected one
                if (selectedStartSlotStr == null)                    // (no selection)
                    return@map Pair(playground, SelectionState.UNSELECTED)

                // set original reservation slots (which are actually busy) as *available* for the current user session
                originalReservationBundle?.let {
                    if (playground.playgroundId == it.getInt("playground_id") &&
                        ((it.getString("end_slot") == null && LocalDateTime.parse(it.getString("start_slot")) == slot) ||
                         (it.getString("end_slot") != null && slot >= LocalDateTime.parse(it.getString("start_slot")) && slot <= LocalDateTime.parse(it.getString("end_slot")))))
                        playground.available = true
                }

                // (a selection is in progress, but this is a different playground)
                if (playground.playgroundId != selectedPlaygroundId)
                    return@map Pair(playground, SelectionState.UNSELECTED)

                // determine if this playground slot has been selected or not,
                // based on the selected start/end time

                val selectedStartSlot = LocalDateTime.parse(selectedStartSlotStr)
                val selectedEndSlot = selectedEndSlotStr?.let {
                    LocalDateTime.parse(it)
                }

                if (selectedEndSlot == null) {
                    // just one slot is selected
                    if (slot == selectedStartSlot) {
                        return@map Pair(playground, SelectionState.SELECTED)
                    }
                    else if (slot > selectedStartSlot && slot.toLocalDate() == selectedStartSlot.toLocalDate()) {
                        val playgroundAlreadyBusyInOnePreviousSlot = playgroundAvailabilities
                            .filterKeys { s -> s > selectedStartSlot && s <= slot } // take the slots between the selected one and this one
                            .any { (_, playgrounds) -> playgrounds.any {
                                p -> p.playgroundId == playground.playgroundId && !p.available
                            } }

                        if(!playgroundAlreadyBusyInOnePreviousSlot)
                            return@map Pair(playground, SelectionState.SELECTABLE)
                    }
                }
                else {
                    // more than one slot is selected
                    if (slot >= selectedStartSlot && slot <= selectedEndSlot) {
                        return@map Pair(playground, SelectionState.SELECTED)
                    }
                }

                Pair(playground, SelectionState.UNSELECTED)
            }
        }

        return playgroundAvailabilitiesSelections
    }
}