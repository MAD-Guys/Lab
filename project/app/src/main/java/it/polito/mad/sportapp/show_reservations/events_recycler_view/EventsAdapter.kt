package it.polito.mad.sportapp.show_reservations.events_recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedReservation

/* Events Adapter */

internal class EventsAdapter : RecyclerView.Adapter<EventsViewHolder>(){

    val events = mutableListOf<DetailedReservation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item_view, parent, false)

        return EventsViewHolder(v)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        holder.bind(events[position])
    }
}