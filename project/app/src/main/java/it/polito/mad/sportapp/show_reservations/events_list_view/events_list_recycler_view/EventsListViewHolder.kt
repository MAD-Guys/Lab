package it.polito.mad.sportapp.show_reservations.events_list_view.events_list_recycler_view

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.room.RoomDetailedReservation
import it.polito.mad.sportapp.application_utilities.formatDuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/* Event View Holder */

internal class EventsListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val containerLayout = view.findViewById<ConstraintLayout>(R.id.event_item_container)
    private val dateTimeContainer = view.findViewById<LinearLayout>(R.id.event_date_time_container)

    private val itemLayout = view.findViewById<LinearLayout>(R.id.event_information_container)
    private val dayText = view.findViewById<TextView>(R.id.event_day_text)
    private val timeText = view.findViewById<TextView>(R.id.event_time_text)
    private val dateText = view.findViewById<TextView>(R.id.event_date_text)

    // event information
    private val sportName = view.findViewById<TextView>(R.id.event_sport_name)
    private val eventMoreInfo = view.findViewById<TextView>(R.id.event_more_info)
    private val eventDuration = view.findViewById<TextView>(R.id.event_duration)

    fun bind(event: RoomDetailedReservation) {

        // set item background color
        if (event.startLocalDateTime.isBefore(LocalDateTime.now())) {
            dateTimeContainer.setBackgroundColor(
                getColor(
                    itemView.context,
                    R.color.past_event_tag_color
                )
            )
            containerLayout.setBackgroundColor(
                getColor(
                    itemView.context,
                    R.color.current_item_highlighted
                )
            )
        } else {
            dateTimeContainer.setBackgroundColor(
                getColor(
                    itemView.context,
                    R.color.event_color
                )
            )
            containerLayout.setBackgroundColor(
                getColor(
                    itemView.context,
                    R.color.white
                )
            )
        }

        // get display width
        val displayMetrics = itemView.context.resources.displayMetrics
        val displayWidth = displayMetrics.widthPixels

        dayText.text = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH).format(event.date)
        dateText.text = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH).format(event.date)
        timeText.text = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH).format(event.startTime)

        val eventInfo = "${event.sportCenterName} - ${event.playgroundName}"

        // set event information
        sportName.text = event.sportName
        eventMoreInfo.text = eventInfo
        eventDuration.text = formatDuration(event.duration)

        // set item components width
        dateText.layoutParams.width = displayWidth / 7
        itemLayout.layoutParams.width = displayWidth / 14 * 9
        eventDuration.layoutParams.width = displayWidth / 14 * 3

        // set item components background
        eventDuration.setBackgroundResource(R.drawable.event_duration_box)

        // set item click listener
        itemLayout.setOnClickListener {

            val navController = Navigation.findNavController(itemView)

            val bundle = bundleOf("id_event" to event.id)

            // navigate to reservation details fragment
            navController.navigate(
                R.id.action_eventsListFragment_to_reservationDetailsFragment,
                bundle
            )
        }

    }

}