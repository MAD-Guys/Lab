package it.polito.mad.sportapp.notifications.recyclerView

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.Notification

/* Notifications View Holder */

internal class NotificationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    // notification variables
    private val notificationContainer =
        view.findViewById<ConstraintLayout>(R.id.notification_layout_container)
    private val notificationUsername = view.findViewById<TextView>(R.id.notification_username)

    fun bind(notification: Notification) {

        // add notification listener
        notificationContainer.setOnClickListener {

            val bundle = bundleOf(
                "id_reservation" to notification.reservationId,
                "timestamp" to notification.timestamp
            )

            Navigation.findNavController(itemView)
                .navigate(R.id.action_notificationsFragment_to_notificationDetailsFragment, bundle)
        }

        notificationUsername.text =
            notification.publicationDate.toString() + " " + notification.publicationTime + " " + notification.description
    }

}