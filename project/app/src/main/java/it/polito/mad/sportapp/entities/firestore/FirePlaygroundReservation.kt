package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.NewReservation
import java.time.Duration
import java.time.LocalDateTime

data class FirePlaygroundReservation(
    val id: String?,
    val playgroundId: String,
    val user: FireUserForPlaygroundReservation,
    val participants: List<FireUserForPlaygroundReservation>?,
    val startDateTime: String,
    val endDateTime: String,
    val totalPrice: Double,
    val additionalRequests: String?,
    val timestamp: String
) {
    /**
     * Serialize the FirePlaygroundReservation object into a Map<String, Any> object
     * to send to the Firestore cloud database
     */
    fun serialize(): Map<String, Any?> {
        return mapOf(
            // no id included in serialization
            "playgroundId" to playgroundId,
            "user" to user.serialize(),
            "participants" to participants?.map{ it.serialize() } ,
            "startDateTime" to startDateTime,
            "endDateTime" to endDateTime,
            "totalPrice" to totalPrice,
            "additionalRequests" to additionalRequests,
            "timestamp" to timestamp
        )
    }

    /**
     * Convert the FirePlaygroundReservation object into a DetailedReservation entity
     * passing the FirePlaygroundSport and the list of FireEquipmentReservationSlot objects
     */
    fun toDetailedReservation(
        firePlaygroundSport: FirePlaygroundSport,
        fireEquipmentReservationSlotList: List<FireEquipmentReservationSlot>
    ): DetailedReservation {
        val detailedReservation = DetailedReservation(
            id!!,
            user.uid,
            user.username,
            firePlaygroundSport.sportCenter.id,
            firePlaygroundSport.sport.id,
            firePlaygroundSport.sport.emoji,
            firePlaygroundSport.sportCenter.name,
            firePlaygroundSport.sportCenter.address,
            firePlaygroundSport.sport.name,
            startDateTime,
            endDateTime,
            firePlaygroundSport.id,
            firePlaygroundSport.playgroundName,
            firePlaygroundSport.pricePerHour.toFloat(),
            totalPrice.toFloat()
        )
        detailedReservation.equipments =
            fireEquipmentReservationSlotList.map { it.toDetailedEquipmentReservation() }
                .toMutableList()
        return detailedReservation
    }

    companion object {
        /**
         * Create a FirePlaygroundReservation object from a NewReservation object
         */
        fun fromNewReservation(
            reservation: NewReservation,
            id: String? = null,
            user: FireUserForPlaygroundReservation,
            participants: List<FireUserForPlaygroundReservation>? = null,
            playgroundPricePerHour: Double
        ): FirePlaygroundReservation {
            //Calculate the total price
            val durationInMinutes =
                Duration.between(reservation.startTime, reservation.endTime).toMinutes()
            var totalPrice = playgroundPricePerHour / 60 * durationInMinutes
            // Adding the price of the equipments
            for (equipment in reservation.selectedEquipments) {
                totalPrice += equipment.unitPrice * equipment.selectedQuantity
            }
            return  FirePlaygroundReservation(
                id,
                reservation.playgroundId,
                user,
                participants,
                reservation.startTime.toString(),
                reservation.endTime.toString(),
                totalPrice,
                reservation.additionalRequests,
                LocalDateTime.now().toString()
            )

        }

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
            val participants =
                rawParticipants.mapNotNull { FireUserForPlaygroundReservation.deserialize(it) }

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
