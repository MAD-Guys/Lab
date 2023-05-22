package it.polito.mad.sportapp.notifications.recyclerView

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* Notifications View Holder */

internal class NotificationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    // notification variables
    private val notificationContainer =
        view.findViewById<ConstraintLayout>(R.id.notification_layout_container)
    private val notificationDescription = view.findViewById<TextView>(R.id.notification_description)
    private val notificationTimestamp = view.findViewById<TextView>(R.id.notification_timestamp)

    fun bind(notification: Notification) {

        when(notification.status.name) {
            "ACCEPTED" -> notificationContainer.setBackgroundResource(R.color.rejected_item_highlighted)
            else -> notificationContainer.setBackgroundResource(R.color.white)
        }

        // add notification listener
        notificationContainer.setOnClickListener {

            val bundle = bundleOf(
                "id_reservation" to notification.reservationId,
                "status" to notification.status.name
            )

            Navigation.findNavController(itemView)
                .navigate(R.id.action_notificationsFragment_to_notificationDetailsFragment, bundle)
        }

        // set notification text
        notificationDescription.text = notification.description
        notificationTimestamp.text = getTimeAgo(notification.timestamp)
    }

    private fun getTimeAgo(timestamp: String): String {

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val date = sdf.parse(timestamp)
        val currentDate = Date()

        val diffInMillis = currentDate.time - date!!.time
        val diffInSeconds = diffInMillis / 1000

        return when (currentDate) {
            date -> formatTimeAgo(diffInSeconds)
            else -> "${diffInSeconds / (60 * 60 * 24)} days ago"
        }
    }

    private fun formatTimeAgo(diffInSeconds: Long): String {
        return when {
            diffInSeconds < 60 -> "Now"
            diffInSeconds < 60 * 60 -> "${diffInSeconds / 60} minutes ago"
            else -> "${diffInSeconds / (60 * 60)} hours ago"
        }
    }

}