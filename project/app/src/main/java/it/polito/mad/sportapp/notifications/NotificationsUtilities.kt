package it.polito.mad.sportapp.notifications

import android.content.Intent
import android.util.Log
import androidx.core.os.bundleOf
import androidx.navigation.NavController
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
    status: String,
    notificationTimestamp: String
) {

    // notification variables
    val tag = "NOTIFICATION TAG"

    val notificationTitle = "New Invitation Received!"
    val notificationMessage = "Someone sent you a new invitation! Check it out!"

    // set the notification receiver
    //TODO: retrieve the receiver's token from db with the receiverUid
    //val receiver = "/topics/${receiverUid}"
    val receiver =
        "fEt9asdMTD2NBlGCqOdZc3:APA91bHTo6qhQ2vMkgcCqrE3kG3bLmhwgN_nrSWEaUkY7ggcVBazwXajw2AKF9PUqtHwcGJYRdurt9OsDSCNRkp434F25DVJYmTesDzU1Ygd4vAARQgwgDzJFWMrtf-f1W_fPBHwKHfY"

    val notification = JSONObject()
    val notificationBody = JSONObject()

    try {
        // create notification body
        notificationBody.put("action", "NEW_INVITATION")
        notificationBody.put("title", notificationTitle)
        notificationBody.put("message", notificationMessage)
        notificationBody.put("id_reservation", reservationId)
        notificationBody.put("status", status)
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

internal fun sendTokenToDatabase(token: String, uid: String) {

    //TODO: send token to database and delete the old one associate to the same user id

    //FirebaseMessaging.getInstance().subscribeToTopic(uid)
}

internal fun manageInvitationNotification(intent: Intent, navController: NavController) {

    // get information from intent
    val reservationId = intent.getIntExtra("id_reservation", -1)
    val notificationStatus = intent.getStringExtra("status") ?: "CANCELED"
    val notificationTimestamp = intent.getStringExtra("timestamp") ?: ""

    val bundle = bundleOf(
        "id_reservation" to reservationId,
        "status" to notificationStatus,
        "timestamp" to notificationTimestamp
    )

    // navigate to reservation details fragment only if the user is logged in
    navController.navigate(
        R.id.action_loginFragment_to_notificationDetailsFragment,
        bundle
    )
}