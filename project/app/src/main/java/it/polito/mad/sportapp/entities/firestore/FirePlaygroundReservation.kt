package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import java.time.LocalDateTime

data class FirePlaygroundReservation(
    val id: String,
    val playgroundId: String,
    val user: FireUserForPlaygroundReservation,
    val participants: List<FireUserForPlaygroundReservation>,
    val startDateTime: String,
    val endDateTime: String,
    val totalPrice: Double,
    val additionalRequests: String,
    val timestamp: String
){
    /**
     * Serialize the FirePlaygroundReservation object into a Map<String, Any> object
     * to send to the Firestore cloud database
     */
    fun serialize(): Map<String, Any?> {
        return mapOf(
            // no id included in serialization
            "playgroundId" to playgroundId,
            "user" to user.serialize(),
            "participants" to participants.map { it.serialize() },
            "startDateTime" to startDateTime,
            "endDateTime" to endDateTime,
            "totalPrice" to totalPrice,
            "additionalRequests" to additionalRequests,
            "timestamp" to timestamp
        )
    }

    companion object{
        /**
         * Create a FirePlaygroundReservation object from raw Map<String,Any> data coming from Firestore
         */
        fun deserialize(id: String, data: Map<String, Any>?): FirePlaygroundReservation? {
            if (data == null) {
                Log.d(
                    "deserialization error",
                    "Error deserializing FirePlaygroundReservation the data passed is null in FirePlaygroundReservation.deserialize()"
                )
                return null
            }

            val playgroundId = data["playgroundId"] as? String
            val rawUser = data["user"] as? Map<String, Any>
            val rawParticipants = data["participants"] as? List<Map<String, Any>>
            val startDateTime = data["startDateTime"] as? String
            val endDateTime = data["endDateTime"] as? String
            val totalPrice = data["totalPrice"] as? Double
            val additionalRequests = data["additionalRequests"] as? String
            val timestamp = data["timestamp"] as? String

            if (playgroundId == null || rawUser == null || rawParticipants == null || startDateTime == null || endDateTime == null || totalPrice == null || additionalRequests == null || timestamp == null) {
                Log.d(
                    "deserialization error",
                    "Error deserializing FirePlaygroundReservation in FirePlaygroundReservation.deserialize()"
                )
                return null
            }

            val user = FireUserForPlaygroundReservation.deserialize(rawUser)
            val participants = rawParticipants.mapNotNull { FireUserForPlaygroundReservation.deserialize(it) }

            return FirePlaygroundReservation(
                id,
                playgroundId,
                user!!,
                participants,
                startDateTime,
                endDateTime,
                totalPrice,
                additionalRequests,
                timestamp
            )
        }
    }
}
