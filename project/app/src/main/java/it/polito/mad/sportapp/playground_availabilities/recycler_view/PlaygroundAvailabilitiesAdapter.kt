package it.polito.mad.sportapp.playground_availabilities.recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import java.lang.Exception
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class PlaygroundAvailabilitiesAdapter(
    // map containing all the slots and their corresponding available playgrounds
    playgroundAvailabilities: Map<LocalDateTime, List<DetailedPlaygroundSport>>,
    internal var selectedDate: LocalDate,
    private val slotDuration: Duration,
    private val navigateToPlayground: (Int) -> Unit
) : RecyclerView.Adapter<AbstractTimeSlotVH>()
{
    // list of all the interesting slots, chronologically ordered
    private var timeSlots = playgroundAvailabilities.keys.toList().sorted()

    internal var playgroundAvailabilities = playgroundAvailabilities
        set(value) {
            field = value
            timeSlots = value.keys.toList().sorted()
        }

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
            R.layout.time_slot_availabilities_container -> TimeSlotVH(timeSlotView, navigateToPlayground)
            R.layout.no_available_playgrounds_box -> NoAvailablePlaygroundsVH(timeSlotView)
            else -> throw Exception("Unexpected view found in onCreateViewHolder")
        }
    }

    override fun onBindViewHolder(holder: AbstractTimeSlotVH, position: Int) {
        if(holder is TimeSlotVH) {
            val timeSlot = timeSlots[position]
            val availablePlaygrounds = playgroundAvailabilities[timeSlot]!!

            holder.setTimeSlotTimes(timeSlot, timeSlot.plus(slotDuration))
            holder.setAvailablePlaygrounds(availablePlaygrounds)
        }
    }

    fun smartUpdatePlaygroundAvailabilities(
        newPlaygroundAvailabilities: Map<LocalDateTime, List<DetailedPlaygroundSport>>
    ) {
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

                return timeSlots[oldItemPosition].toLocalTime() == newTimeSlots[newItemPosition].toLocalTime()
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
                val oldPlaygrounds = playgroundAvailabilities[oldTimeSlot]!!

                val newTimeSlots = newPlaygroundAvailabilities.keys.toList().sorted()
                val newTimeSlot = newTimeSlots[newItemPosition]
                val newPlaygrounds = newPlaygroundAvailabilities[newTimeSlot]!!

                // check exact equality between the two lists
                return oldPlaygrounds.all{ oldPlayground ->
                            newPlaygrounds.any{ newPlayground ->
                                oldPlayground.exactlyEqualTo(newPlayground)
                            } &&
                       newPlaygrounds.all { newPlayground ->
                            oldPlaygrounds.any { oldPlayground ->
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
}