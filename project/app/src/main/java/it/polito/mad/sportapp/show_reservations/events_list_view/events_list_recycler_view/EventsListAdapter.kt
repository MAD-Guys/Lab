package it.polito.mad.sportapp.show_reservations.events_list_view.events_list_recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedReservation

/* Events List Adapter */

internal class EventsListAdapter : RecyclerView.Adapter<EventsListViewHolder>(){

    val events = mutableListOf<DetailedReservation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsListViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item_view, parent, false)

        return EventsListViewHolder(v)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventsListViewHolder, position: Int) {
        holder.bind(events[position])
    }
}