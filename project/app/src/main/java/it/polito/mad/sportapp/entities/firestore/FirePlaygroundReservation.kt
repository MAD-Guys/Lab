package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.NewReservation
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class FirePlaygroundReservation(
    val id: String?,
    val playgroundId: String,
    val user: FireUserForPlaygroundReservation,
    val participants: List<FireUserForPlaygroundReservation>,
    val startDateTime: String,
    val endDateTime: String,
    val totalPrice: Double,
    val additionalRequests: String?,
    val timestamp: String
) {
    /**
     * Serialize the FirePlaygroundReservation object into a Map<String, Any>
     * object to send to the Firestore cloud database
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

    /**
     * Convert the FirePlaygroundReservation object into a DetailedReservation
     * entity passing the FirePlaygroundSport and the list of
     * FireEquipmentReservationSlot objects
     */
    fun toDetailedReservation(
        firePlaygroundSport: FirePlaygroundSport,
        fireEquipmentReservationSlotList: List<FireEquipmentReservationSlot>
    ): DetailedReservation {
        val detailedReservation = DetailedReservation(
            id!!,
            user.id,
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
            additionalRequests,
            totalPrice.toFloat(),
            participants.map { it.username }
        )
        detailedReservation.equipments =
            fireEquipmentReservationSlotList.map { it.toDetailedEquipmentReservation() }
                .toMutableList()
        return detailedReservation
    }

    companion object {
        /** Create a FirePlaygroundReservation object from a NewReservation object */
        fun fromNewReservation(
            reservation: NewReservation,
            user: FireUserForPlaygroundReservation,
        ): FirePlaygroundReservation {
            // Calculate the total price
            val durationInMinutes =
                Duration.between(reservation.startTime, reservation.endTime).toMinutes()
            val durationInHours = durationInMinutes.toDouble() / 60.0
            var totalPrice = reservation.playgroundPricePerHour * durationInHours

            // Adding the price of the equipments
            for (equipment in reservation.selectedEquipments) {
                totalPrice += equipment.unitPrice.toDouble() * equipment.selectedQuantity.toDouble()
            }

            // create participants list with the owner only, even if this is an existing reservation
            val participants = mutableListOf<FireUserForPlaygroundReservation>().also {
                it.add(user)
            }

            return FirePlaygroundReservation(
                reservation.id,
                reservation.playgroundId,
                user,
                participants,
                reservation.startTime.format(DateTimeFormatter.ISO_DATE_TIME),
                reservation.endTime.format(DateTimeFormatter.ISO_DATE_TIME),
                totalPrice,
                reservation.additionalRequests,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            )
        }

        /**
         * Create a FirePlaygroundReservation object from raw Map<String,Any?> data
         * coming from Firestore
         */
        fun deserialize(id: String, data: Map<String, Any?>?): FirePlaygroundReservation? {
            if (data == null) {
                Log.e(
                    "deserialization error",
                    "Error deserializing FirePlaygroundReservation the data passed is null in FirePlaygroundReservation.deserialize()"
                )
                return null
            }

            val playgroundId = data["playgroundId"] as? String

            @Suppress("UNCHECKED_CAST")
            val rawUser = data["user"] as? Map<String, Any>
            val rawParticipants = data["participants"] as? List<*>
            val startDateTime = data["startDateTime"] as? String
            val endDateTime = data["endDateTime"] as? String
            val totalPrice =
                (data["totalPrice"] as? Double) ?: (data["totalPrice"] as? Long)?.toDouble()
            val additionalRequests = data["additionalRequests"] as? String?
            val timestamp = data["timestamp"] as? String

            if (playgroundId == null || rawUser == null || rawParticipants == null ||
                startDateTime == null || endDateTime == null || totalPrice == null ||
                timestamp == null
            ) {
                Log.e(
                    "deserialization error",
                    "Error deserializing FirePlaygroundReservation plain properties in FirePlaygroundReservation.deserialize()"
                )
                return null
            }

            val user = FireUserForPlaygroundReservation.deserialize(rawUser)

            if (user == null) {
                Log.e(
                    "deserialization error",
                    "Error deserializing user in FirePlaygroundReservation.deserialize()"
                )
                return null
            }

            val participants = mutableListOf<FireUserForPlaygroundReservation>()

            for (rawParticipant in rawParticipants) {
                @Suppress("UNCHECKED_CAST")
                val participantMap = rawParticipant as? Map<String, Any>
                val participant = FireUserForPlaygroundReservation.deserialize(participantMap)

                if (participant == null) {
                    Log.e(
                        "deserialization error",
                        "Error deserializing participant in FirePlaygroundReservation.deserialize()"
                    )
                    return null
                }

                participants.add(participant)
            }

            return FirePlaygroundReservation(
                id,
                playgroundId,
                user,
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
