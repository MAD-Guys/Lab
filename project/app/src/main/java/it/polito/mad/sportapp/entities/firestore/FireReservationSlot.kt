package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.NewReservation
import java.time.Duration
import java.time.format.DateTimeFormatter

data class FireReservationSlot(
    val id: String?,
    val startSlot: String,
    val endSlot: String,
    val playgroundId: String,
    val openPlaygroundsIds: List<String>,
    val reservationId: String
) {
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

    companion object {

        /**
         * Starting from a NewReservation object, create a list of FireReservationSlot objects
         * subdividing the reservation into slots
         */
        fun fromNewReservation(
            newReservation: NewReservation,
            reservationId: String,
        ): List<FireReservationSlot>
        {
            val fireReservationSlots = mutableListOf<FireReservationSlot>()

            // Computing the duration useful to retrieve the number of slots needed for the reservation
            val durationInMinutes =
                Duration.between(newReservation.startTime, newReservation.endTime).toMinutes()

            // Creating a FireReservationSlot object for each slot every 30 minutes
            for (i in 0 until durationInMinutes step 30) {
                fireReservationSlots.add(
                    FireReservationSlot(
                        null,
                        newReservation.startTime.plusMinutes(i).format(DateTimeFormatter.ISO_DATE_TIME),
                        newReservation.startTime.plusMinutes(i + 30).format(DateTimeFormatter.ISO_DATE_TIME),
                        newReservation.playgroundId,
                        openPlaygroundsIds=mutableListOf(), // TODO: * temporary empty list * -> fill it after in the repository
                        reservationId
                    )
                )
            }
            return fireReservationSlots
        }

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
            val rawOpenPlaygroundsIds = data["openPlaygroundsIds"] as? List<*>
            val reservationId = data["reservationId"] as? String

            if (startSlot == null || endSlot == null || playgroundId == null ||
                rawOpenPlaygroundsIds == null || reservationId == null) {
                Log.d(
                    "deserialization error",
                    "Error deserializing FireReservationSlot properties in FireReservationSlot.deserialize()"
                )
                return null
            }

            val openPlaygroundIds = mutableListOf<String>()

            for (rawOpenPlaygroundId in rawOpenPlaygroundsIds) {
                val openPlaygroundId = rawOpenPlaygroundId as? String

                if(openPlaygroundId == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error: error deserializing open playground id in FireReservationSlot.deserialize()")
                    return null
                }

                openPlaygroundIds.add(openPlaygroundId)
            }

            return FireReservationSlot(
                id,
                startSlot,
                endSlot,
                playgroundId,
                openPlaygroundIds,
                reservationId
            )
        }
    }
}
