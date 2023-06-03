package it.polito.mad.sportapp.notifications

import android.content.Intent
import android.util.Log
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.sportapp.R

/* NOTIFICATION UTILITIES */

internal fun manageNotification(activityIntent: Intent?, navController: NavController) {

    // check if the activity has an intent
    if (activityIntent != null) {

        when (activityIntent.action?.lowercase()) {
            "invitation" -> {
                // get information from intent
                val notificationId = activityIntent.getStringExtra("notification_id")
                val reservationId = activityIntent.getStringExtra("reservation_id")
                val notificationStatus = activityIntent.getStringExtra("status") ?: "CANCELED"
                val notificationTimestamp = activityIntent.getStringExtra("timestamp") ?: ""

                val bundle = bundleOf(
                    "notification_id" to notificationId,
                    "reservation_id" to reservationId,
                    "status" to notificationStatus,
                    "timestamp" to notificationTimestamp
                )

                // insert fragments into the navigation the back stack
                navController.navigate(R.id.showReservationsFragment)
                navController.navigate(R.id.notificationsFragment)

                // navigate to notificationDetails fragment
                navController.navigate(
                    R.id.action_notificationsFragment_to_notificationDetailsFragment,
                    bundle
                )
            }

            "invitation_accepted", "invitation_declined", "invitation_rejected" -> {

                // get information from intent
                val reservationId = activityIntent.getStringExtra("reservation_id")

                val bundle = bundleOf(
                    "id_event" to reservationId
                )

                // insert fragments into the navigation the back stack
                navController.navigate(R.id.showReservationsFragment)

                // navigate to reservation details fragment
                navController.navigate(
                    R.id.action_showReservationsFragment_to_reservationDetailsFragment,
                    bundle
                )
            }

            else -> {
                // navigate to showReservations fragment
                navController.navigate(R.id.showReservationsFragment)
            }
        }
    } else {
        // navigate to showReservations fragment
        navController.navigate(R.id.showReservationsFragment)
    }
}

/* tokens */

internal fun deleteCurrentToken() {
    FirebaseMessaging.getInstance().deleteToken()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("DELETE TOKEN", "Token $task successfully deleted!")
            } else {
                Log.e("DELETE TOKEN", "Token deletion failed!")
            }
        }
}