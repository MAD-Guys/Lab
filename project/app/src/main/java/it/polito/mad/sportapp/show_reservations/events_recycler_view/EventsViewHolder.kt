package it.polito.mad.sportapp.show_reservations.events_recycler_view

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.show_reservations.*

internal class EventsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemLayout = view.findViewById<LinearLayout>(R.id.item_layout)
    private val dateText = view.findViewById<TextView>(R.id.event_date_text)
    private val sportName = view.findViewById<TextView>(R.id.event_sport_name)
    private val sportDuration = view.findViewById<TextView>(R.id.event_duration)

    fun bind(event: Event) {

        dateText.apply {
            text = eventDateTimeFormatter.format(event.time)
        }

        sportName.text = event.sportName
        sportDuration.text = event.sportDuration

        // set item click listener
        itemLayout.setOnClickListener {
            Log.d("DAY: ${event.time} - SPORT: ${event.sportName}", "Item Clicked!")
        }

    }

}