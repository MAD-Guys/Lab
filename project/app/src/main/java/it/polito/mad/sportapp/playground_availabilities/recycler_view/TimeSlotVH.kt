package it.polito.mad.sportapp.playground_availabilities.recycler_view

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeSlotVH(
    val view: View,
    private val navigateToPlayground: (Int) -> Unit
) : AbstractTimeSlotVH(view)
{
    private val startTimeSlotText = view.findViewById<TextView>(R.id.start_time_slot)
    private val endTimeSlotText = view.findViewById<TextView>(R.id.end_time_slot)
    private val availablePlaygroundsContainer =
        view.findViewById<LinearLayout>(R.id.available_playgrounds_container)

    fun setTimeSlotTimes(start: LocalDateTime, end: LocalDateTime) {
        startTimeSlotText.text = start.format(DateTimeFormatter.ofPattern("HH:mm"))
        endTimeSlotText.text = end.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    fun setAvailablePlaygrounds(availablePlaygrounds: List<DetailedPlaygroundSport>) {
        availablePlaygroundsContainer.removeAllViews()  // clear playgrounds

        // transform each available playground object in a box view
        val availablePlaygroundBoxes = availablePlaygrounds.map { playground ->
            val playgroundBox = LayoutInflater.from(view.context).inflate(
                if(playground.available) R.layout.available_playground_item else R.layout.unavailable_playground_item,
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
                // TODO: manage edit/add mode
                navigateToPlayground(playground.playgroundId)
            }

            playgroundBox
        }

        // push the playgrounds boxes into the container
        availablePlaygroundBoxes.forEach { availablePlaygroundsContainer.addView(it) }
    }
}