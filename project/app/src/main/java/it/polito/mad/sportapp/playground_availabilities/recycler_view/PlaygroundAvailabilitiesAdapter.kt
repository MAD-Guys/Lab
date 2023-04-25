package it.polito.mad.sportapp.playground_availabilities.recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.model.DetailedPlaygroundSport
import java.time.Duration
import java.time.LocalDateTime

class PlaygroundAvailabilitiesAdapter(
    playgroundAvailabilities: Map<LocalDateTime, List<DetailedPlaygroundSport>>,
    private val slotDuration: Duration) : RecyclerView.Adapter<TimeSlotVH>()
{
    // map containing slots and their corresponding available playgrounds
    var playgroundAvailabilities = playgroundAvailabilities
        set(newValue) {
            field = newValue
            timeSlots = newValue.keys.toList().sorted() // also update the slots
        }

    // ordered list of all the slots
    private var timeSlots = playgroundAvailabilities.keys.toList().sorted()


    override fun getItemCount(): Int {
        return playgroundAvailabilities.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotVH {
        val timeSlotView = LayoutInflater.from(parent.context).inflate(
            R.layout.time_slot_availabilities_container, parent, false)

        return TimeSlotVH(timeSlotView)
    }

    override fun onBindViewHolder(holder: TimeSlotVH, position: Int) {
        val timeSlot = timeSlots[position]
        val availablePlaygrounds = playgroundAvailabilities[timeSlot].orEmpty()

        holder.setTimeSlotTimes(timeSlot, timeSlot.plus(slotDuration))
        holder.setAvailablePlaygrounds(availablePlaygrounds)
    }
}