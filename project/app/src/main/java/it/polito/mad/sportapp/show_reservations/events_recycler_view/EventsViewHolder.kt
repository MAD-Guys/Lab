package it.polito.mad.sportapp.show_reservations.events_recycler_view

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.formatDuration
import it.polito.mad.sportapp.reservation_details.ReservationDetailsActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/* Event View Holder */

internal class EventsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val containerLayout = view.findViewById<ConstraintLayout>(R.id.event_item_container)

    private val itemLayout = view.findViewById<LinearLayout>(R.id.event_information_container)
    private val dayText = view.findViewById<TextView>(R.id.event_day_text)
    private val timeText = view.findViewById<TextView>(R.id.event_time_text)
    private val dateText = view.findViewById<TextView>(R.id.event_date_text)

    // event information
    private val sportName = view.findViewById<TextView>(R.id.event_sport_name)
    private val eventMoreInfo = view.findViewById<TextView>(R.id.event_more_info)
    private val eventDuration = view.findViewById<TextView>(R.id.event_duration)

    fun bind(event: DetailedReservation) {

        // set item background color
        if (event.date.isBefore(LocalDate.now())) {
            containerLayout.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.past_event_color
                )
            )
        } else {
            containerLayout.setBackgroundColor(
                ContextCompat.getColor(
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

            //create the intent
            val intent = Intent(it.context, ReservationDetailsActivity::class.java)
            intent.putExtra("id_event", event.id)

            //start the Reservation Details Activity
            startActivity(it.context, intent, null)
        }

    }
}