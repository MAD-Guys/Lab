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
        if (activityIntent.action?.lowercase() == "invitation") {
            // get information from intent
            val reservationId = activityIntent.getIntExtra("id_reservation", -1)
            val notificationStatus = activityIntent.getStringExtra("status") ?: "CANCELED"
            val notificationTimestamp = activityIntent.getStringExtra("timestamp") ?: ""

            val bundle = bundleOf(
                "id_reservation" to reservationId,
                "status" to notificationStatus,
                "timestamp" to notificationTimestamp
            )

            // insert fragments in navigation the back stack
            navController.navigate(R.id.showReservationsFragment)
            navController.navigate(R.id.notificationsFragment)

            // navigate to notificationDetails fragment
            navController.navigate(
                R.id.action_notificationsFragment_to_notificationDetailsFragment,
                bundle
            )
        } else {
            // navigate to showReservations fragment
            navController.navigate(R.id.showReservationsFragment)
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