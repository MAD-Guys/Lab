package it.polito.mad.sportapp.show_reservations.events_recycler_view

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.reservation_details.ReservationDetailsActivity
import it.polito.mad.sportapp.show_reservations.*

/* Event View Holder */

internal class EventsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemLayout = view.findViewById<LinearLayout>(R.id.event_information_container)
    private val dayText = view.findViewById<TextView>(R.id.event_day_text)
    private val timeText = view.findViewById<TextView>(R.id.event_time_text)
    private val dateText = view.findViewById<TextView>(R.id.event_date_text)

    // event information
    private val sportName = view.findViewById<TextView>(R.id.event_sport_name)
    private val eventMoreInfo = view.findViewById<TextView>(R.id.event_more_info)
    private val eventDuration = view.findViewById<TextView>(R.id.event_duration)

    fun bind(event: Event) {

        // get display width
        val displayMetrics = itemView.context.resources.displayMetrics
        val displayWidth = displayMetrics.widthPixels

        // format event day, date and time
        val eventDateTime = eventDateTimeFormatter.format(event.time).split(" ")

        dayText.text = eventDateTime[0]
        dateText.text = eventDateTime[1]
        timeText.text = eventDateTime[2]

        val eventInfo = "${event.sportCenterName} - ${event.sportPlaygroundName}"

        // set event information
        sportName.text = event.sportName
        eventMoreInfo.text = eventInfo
        eventDuration.text = event.sportDuration

        // set item components width
        dateText.layoutParams.width = displayWidth / 7
        itemLayout.layoutParams.width = displayWidth / 14 * 9
        eventDuration.layoutParams.width = displayWidth / 14 * 3

        // set item components background
        eventDuration.setBackgroundResource(R.drawable.event_duration_box)

        // set item click listener
        itemLayout.setOnClickListener {

            //create the intent
            val intent = Intent(it.context, ReservationDetailsActivity::class.java)
            intent.putExtra("id_event", event.id)

            //start the Reservation Details Activity
            startActivity(it.context, intent, null)
        }

    }

}