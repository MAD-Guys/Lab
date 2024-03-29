package it.polito.mad.sportapp.notifications.recyclerView

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.application_utilities.CircleTransform
import it.polito.mad.sportapp.entities.Notification
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/* Notifications View Holder */

internal class NotificationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    // notification variables
    private val notificationContainer =
        view.findViewById<ConstraintLayout>(R.id.notification_item_container)
    private val notificationIcon = view.findViewById<ImageView>(R.id.notification_icon)
    private val notificationDescription = view.findViewById<TextView>(R.id.notification_description)
    private val notificationTimestamp = view.findViewById<TextView>(R.id.notification_timestamp)

    fun bind(notification: Notification) {

        // get display width
        val displayMetrics = itemView.context.resources.displayMetrics
        val displayWidth = displayMetrics.widthPixels

        val typefacePoppinsMedium = ResourcesCompat.getFont(itemView.context, R.font.poppins_medium)
        val typefacePoppinsSemibold =
            ResourcesCompat.getFont(itemView.context, R.font.poppins_semibold)

        // set item components width
        notificationIcon.layoutParams.width = displayWidth / 14 * 3
        notificationDescription.layoutParams.width = displayWidth / 14 * 10
        notificationTimestamp.layoutParams.width = displayWidth / 14 * 1

        when (notification.status.name) {
            "PENDING" -> {
                notificationContainer.setBackgroundResource(R.color.pending_notification_highlighted)
                notificationDescription.typeface = typefacePoppinsSemibold
            }

            else -> {
                notificationContainer.setBackgroundResource(R.color.white)
                notificationDescription.typeface = typefacePoppinsMedium
            }
        }

        // add notification listener
        notificationContainer.setOnClickListener {

            val bundle = bundleOf(
                "notification_id" to notification.id,
                "reservation_id" to notification.reservationId,
                "status" to notification.status.name,
                "timestamp" to notification.timestamp
            )

            Navigation.findNavController(itemView)
                .navigate(R.id.action_notificationsFragment_to_notificationDetailsFragment, bundle)
        }

        if (notification.profileUrl != null) {
            // set notification icon with url
            Picasso.get()
                .load(notification.profileUrl)
                .transform(CircleTransform())
                .into(notificationIcon)
        } else {
            // set notification icon with default url
            Picasso.get()
                .load(R.drawable.user_profile_picture_noalpha)
                .transform(CircleTransform())
                .into(notificationIcon)
        }

        // set text in notification text views
        notificationDescription.text = notification.description
        notificationTimestamp.text = getDaysAgo(notification.timestamp)
    }

    private fun getDaysAgo(timestamp: String): String {

        val currentDate = LocalDate.now()
        val returnValue: String

        val notificationTimestamp = LocalDateTime.parse(timestamp)
        val notificationDate = notificationTimestamp.toLocalDate()
        val notificationTime = notificationTimestamp.toLocalTime()

        return when {
            notificationDate.isBefore(currentDate) -> {

                val difference =
                    Duration.between(notificationDate.atStartOfDay(), currentDate.atStartOfDay())
                        .toDays()

                returnValue = if (difference < 7L) {
                    "$difference" + "d"
                } else {
                    "${difference / 7}" + "w"
                }

                returnValue
            }

            notificationDate.isEqual(currentDate) -> {
                getTimeAgo(notificationTime)
            }

            else -> throw RuntimeException("It must not exists a notification date after $currentDate")
        }
    }

    private fun getTimeAgo(notificationTime: LocalTime): String {

        val currentTime = LocalTime.now()
        val returnValue: String

        return when {
            notificationTime.format(DateTimeFormatter.ISO_TIME) <= currentTime.format(
                DateTimeFormatter.ISO_TIME
            ) -> {
                val difference =
                    Duration.between(notificationTime, currentTime).toMillis() / 1000L / 60L

                returnValue = if (difference < 1L) {
                    "Now"
                } else if (difference < 60L) {
                    "$difference" + "m"
                } else {
                    "${difference / 60}" + "h"
                }

                returnValue
            }

            else -> "Now"
        }
    }

}