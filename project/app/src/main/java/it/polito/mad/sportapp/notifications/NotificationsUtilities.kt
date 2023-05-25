package it.polito.mad.sportapp.notifications

import android.content.Intent
import android.util.Log
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.sportapp.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/* NOTIFICATION UTILITIES */

internal fun createInvitationNotification(
    receiverUid: String,
    reservationId: Int,
    notificationDescription: String,
    notificationTimestamp: String
) {

    // notification variables
    val tag = "NOTIFICATION TAG"

    val notificationTitle = "New Invitation"

    // set the notification receiver
    //TODO: retrieve the receiver's token from db with the receiverUid
    val receiver =
        "ctFun_SCT5-oph334SVeZW:APA91bFjQV_eqaXo0MP6RZWf8dr7qaNDjC62uOTXBTT4alLQYsfhIQaGA_lhndivZGqvodh-7ZNLognzLUVjksPJM0VYeK-iT-uMTkHv8Fr8ooBSF3OxVyRWxVBroN_Jgi35zufQ1Ea4"

    val notification = JSONObject()
    val notificationBody = JSONObject()

    try {
        // create notification body
        notificationBody.put("action", "NEW_INVITATION")
        notificationBody.put("title", notificationTitle)
        notificationBody.put("message", notificationDescription)
        notificationBody.put("id_reservation", reservationId)
        notificationBody.put("status", "PENDING")
        notificationBody.put("timestamp", notificationTimestamp)

        // create notification
        notification.put("to", receiver)
        notification.put("data", notificationBody)
    } catch (e: JSONException) {
        Log.e(tag, "createInvitationNotification function: " + e.message)
    }

    // send notification
    sendInvitationNotification(notification)
}

internal fun sendInvitationNotification(notification: JSONObject) {

    // API variables
    val fcmAPI = "https://fcm.googleapis.com/fcm/send"
    val serverKey =
        "key=" + "AAAAEgeVTRw:APA91bH_I9ilwfS5o7n3U45BdKy2TQiHlBEqzbP0hONdx7IFbn1PgZdIEOk3GoMSVpQWGzKJ4so5ax50wW7hHFBuZsyVXcgp8hyM3EAqZtzSn99F5ntvV4aDht3Zl4TK5bwoWipF_9IH"
    val contentType = "application/json"

    // create request
    val request = Request.Builder()
        .url(fcmAPI)
        .post(RequestBody.create(MediaType.parse(contentType), notification.toString()))
        .addHeader("Authorization", serverKey)
        .build()

    // Send the request
    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("SEND INVITATION NOTIFICATION", "Notification sending failed! ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            // Handle request success
            if (response.isSuccessful) {
                Log.i("SEND INVITATION NOTIFICATION", "Notification successfully sent!")
            } else {
                Log.e("SEND INVITATION NOTIFICATION", "Notification sending failed!")
            }
        }
    })
}

internal fun manageNotification(activityIntent: Intent?, navController: NavController) {

    // check if the activity has an intent
    if (activityIntent != null) {
        if (activityIntent.action == "NEW_INVITATION") {
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
internal fun sendTokenToDatabase(token: String, uid: String) {

    //TODO: send token to database and delete the old one associate to the same user id

}

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