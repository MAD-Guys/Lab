package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import java.time.LocalDateTime

data class FireReservationSlot(
    val id: String,
    val startSlot: String,
    val endSlot: String,
    val playgroundId: String,
    val openPlaygroundsIds: List<String>,
    val reservationId: String
){
    /**
     * Serialize the FireReservationSlot object into a Map<String, Any> object
     * to send to the Firestore cloud database
     */
    fun serialize(): Map<String, Any?> {
        return mapOf(
            // no id included in serialization
            "startSlot" to startSlot,
            "endSlot" to endSlot,
            "playgroundId" to playgroundId,
            "openPlaygroundsIds" to openPlaygroundsIds,
            "reservationId" to reservationId
        )
    }

    companion object{
        /**
         * Create a FireReservationSlot object from raw Map<String,Any> data coming from Firestore
         */
        fun deserialize(id: String, data: Map<String, Any>?): FireReservationSlot? {
            if (data == null) {
                // deserialization error
                Log.d(
                    "deserialization error",
                    "Error deserializing FireReservationSlot the data passed is null in FireReservationSlot.deserialize()"
                )
                return null
            }

            val startSlot = data["startSlot"] as? String
            val endSlot = data["endSlot"] as? String
            val playgroundId = data["playgroundId"] as? String
            val openPlaygroundsIds = data["openPlaygroundsIds"] as? List<String>
            val reservationId = data["reservationId"] as? String

            if (startSlot == null || endSlot == null || playgroundId == null || openPlaygroundsIds == null || reservationId == null) {
                Log.d(
                    "deserialization error",
                    "Error deserializing FireReservationSlot the passed is null in FireReservationSlot.deserialize()"
                )
                return null
            }

            return FireReservationSlot(
                id,
                startSlot,
                endSlot,
                playgroundId,
                openPlaygroundsIds,
                reservationId
            )
        }
    }

}
