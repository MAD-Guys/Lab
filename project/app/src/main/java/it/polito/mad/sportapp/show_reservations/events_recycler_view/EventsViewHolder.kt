package it.polito.mad.sportapp.show_reservations.events_recycler_view

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.show_reservations.*

internal class EventsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val dateText = view.findViewById<TextView>(R.id.event_date_text)
    private val sportName = view.findViewById<TextView>(R.id.event_sport_name)
    private val sportDuration = view.findViewById<TextView>(R.id.event_duration)

    // event info icon
    private val infoIcon = view.findViewById<ImageView>(R.id.event_info_icon)

    // show details button
    private val showDetailsButton = view.findViewById<Button>(R.id.show_details_button)

    fun bind(event: Event) {

        dateText.apply {
            text = eventDateTimeFormatter.format(event.time)
            setBackgroundColor(itemView.context.getColor(event.color))
        }

        sportName.text = event.sportName
        sportDuration.text = event.sportDuration

        // set info icon tint
        infoIcon.setColorFilter(itemView.context.getColor(event.color))

        // set button background color
        showDetailsButton.setBackgroundColor(itemView.context.getColor(event.color))

        // set show details button listener
        showDetailsButton.setOnClickListener {
            Log.d("EVENT: ${event.sportName}", "Show details button clicked!")
        }

    }

}