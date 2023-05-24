package it.polito.mad.sportapp.notifications.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.room.RoomNotification

/* Notifications Adapter */

internal class NotificationsAdapter : RecyclerView.Adapter<NotificationsViewHolder>() {

    val notifications = mutableListOf<RoomNotification>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_layout, parent, false)

        return NotificationsViewHolder(v)
    }

    override fun getItemCount(): Int = notifications.size

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        holder.bind(notifications[position])
    }
}