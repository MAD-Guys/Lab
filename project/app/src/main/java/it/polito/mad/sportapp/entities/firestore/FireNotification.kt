package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.NotificationStatus
import java.lang.Exception
import java.time.format.DateTimeParseException

data class FireNotification(
    val id: String?,
    val type: FireNotificationType,
    val reservationId: String,
    val senderId: String,
    val receiverId: String,
    val profileUrl: String?,
    val status: FireNotificationStatus,
    val description: String,
    val timestamp: String,
) {
    /**
     * Serialize fireNotification to raw Map<String,Any?> data to be sent to
     * Firestore cloud db
     */
    fun serialize(): Map<String, Any?> {
        return mapOf(
            // no id in serialization
            "type" to type.index,
            "reservationId" to reservationId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "profileUrl" to profileUrl,
            "status" to status.index,
            "description" to description,
            "timestamp" to timestamp
        )
    }

    /**
     * Convert fireNotification to Notification entity; returns null if the
     * conversion fails
     */
    fun toNotification(): Notification? {
        try {
            return Notification(
                id,
                type.type,
                reservationId,
                senderId,
                receiverId,
                profileUrl,
                NotificationStatus.values()[status.ordinal],
                description,
                timestamp
            )
        } catch (e: DateTimeParseException) {
            Log.e(
                "conversion error",
                "Error: error parsing dates in converting fireNotification to notification in FireNotification.toNotification()"
            )
            return null
        } catch (e: Exception) {
            Log.e(
                "conversion error",
                "Error: generic error converting fireNotification to notification in FireNotification.toNotification()"
            )
            return null
        }
    }

    companion object {
        /**
         * Deserialize raw Map<String,Any?> data coming from Firestore cloud db and
         * return the corresponding fireNotification
         */
        fun deserialize(id: String, data: Map<String, Any?>?): FireNotification? {
            if (data == null) {
                Log.e(
                    "deserialization error",
                    "Error: null data deserializing notification with id $id in FireNotification.deserialize()"
                )
                return null
            }

            val type = FireNotificationType.of((data["type"] as? Long))
            val reservationId = data["reservationId"] as? String
            val senderId = data["senderId"] as? String
            val receiverId = data["receiverId"] as? String
            val profileUrl = data["profileUrl"] as? String?
            val status = FireNotificationStatus.of(data["status"] as? Long)
            val description = data["description"] as? String
            val timestamp = data["timestamp"] as? String

            if (type == null || reservationId == null || senderId == null || receiverId == null ||
                status == null || description == null || timestamp == null
            ) {
                Log.e(
                    "deserialization error",
                    "Error: null properties deserializing notification with id $id in FireNotification.deserialize()"
                )
                return null
            }

            // create notification object
            return FireNotification(
                id,
                type,
                reservationId,
                senderId,
                receiverId,
                profileUrl,
                status,
                description,
                timestamp
            )
        }

        /** Convert a notification entity to a FireNotification document object */
        fun from(notification: Notification): FireNotification? {
            val notificationStatus =
                FireNotificationStatus.of(notification.status.ordinal.toLong()) ?: return null
            val notificationType = try {
                FireNotificationType.valueOf(notification.type.uppercase())
            } catch (e: Exception) {
                return null
            }

            return FireNotification(
                notification.id,
                notificationType,
                notification.reservationId,
                notification.senderUid,
                notification.receiverUid,
                notification.profileUrl,
                notificationStatus,
                notification.description,
                notification.timestamp
            )
        }
    }

}

enum class FireNotificationType(val type: String) {
    INVITATION("invitation"),
    INVITATION_ACCEPTED("invitation_accepted"),
    INVITATION_DECLINED("invitation_declined"),
    INVITATION_REJECTED("invitation_rejected");

    val index = ordinal

    companion object {
        fun of(rawNotificationType: Long?): FireNotificationType? {
            if (rawNotificationType == null || rawNotificationType > FireNotificationType.values().size)
                return null

            return FireNotificationType.values()[rawNotificationType.toInt()]
        }
    }
}

enum class FireNotificationStatus(val status: String) {
    ACCEPTED("Accepted"),
    CANCELED("Canceled"),
    PENDING("Pending"),
    REJECTED("Rejected");

    val index = ordinal.toLong()

    companion object {
        fun of(rawStatus: Long?): FireNotificationStatus? {
            if (rawStatus == null || rawStatus > FireNotificationStatus.values().size)
                return null

            return FireNotificationStatus.values()[rawStatus.toInt()]
        }
    }
}
