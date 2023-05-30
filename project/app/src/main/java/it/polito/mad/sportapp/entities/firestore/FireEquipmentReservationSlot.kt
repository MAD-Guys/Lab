package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import it.polito.mad.sportapp.entities.NewReservation
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class FireEquipmentReservationSlot(
    val id: String?,
    val startSlot: String,
    val endSlot: String,
    val date: String,
    val equipment: FireEquipment,
    val selectedQuantity: Long,
    val playgroundReservationId: String,
    val timestamp: String
) {
    /**
     * Serialize the FireEquipmentReservationSlot object into a Map<String,
     * Any> object to send to the Firestore cloud database
     */
    fun serialize(): Map<String, Any?> {
        return mapOf(
            // no id included in serialization
            "startSlot" to startSlot,
            "endSlot" to endSlot,
            "date" to date,
            "equipment" to equipment.serialize(withId = true),
            "selectedQuantity" to selectedQuantity,
            "playgroundReservationId" to playgroundReservationId,
            "timestamp" to timestamp
        )
    }

    /**
     * This method converts a FireEquipmentReservationSlot object into a
     * DetailedEquipmentReservation entity
     */
    fun toDetailedEquipmentReservation(): DetailedEquipmentReservation {
        return DetailedEquipmentReservation(
            playgroundReservationId,
            equipment.id,
            equipment.name,
            selectedQuantity.toInt(),
            equipment.unitPrice.toFloat(),
            (equipment.unitPrice.toFloat() * selectedQuantity.toInt())
        )
    }

    companion object {
        /**
         * This method deserializes a Map<String, Any> object coming from Firestore
         * DB into a FireEquipmentReservationSlot object; it returns 'null' if the
         * deserialization fails
         */
        fun deserialize(id: String, fireMap: Map<String, Any>?): FireEquipmentReservationSlot? {
            if (fireMap == null) {
                // deserialization error
                Log.e(
                    "deserialization error",
                    "trying to deserialize a equipmentReservationSlot with null data in FireEquipmentReservationSlot.deserialize()"
                )
                return null
            }

            val startSlot = fireMap["startSlot"] as? String
            val endSlot = fireMap["endSlot"] as? String
            val date = fireMap["date"] as? String

            @Suppress("UNCHECKED_CAST")
            val fireMapEquipment = fireMap["equipment"] as? Map<String, Any>
            val selectedQuantity = fireMap["selectedQuantity"] as? Long
            val playgroundReservationId = fireMap["playgroundReservationId"] as? String
            val timestamp = fireMap["timestamp"] as? String

            if (startSlot == null || endSlot == null || date == null || fireMapEquipment == null ||
                selectedQuantity == null || playgroundReservationId == null || timestamp == null
            ) {
                Log.e(
                    "deserialization error",
                    "Error deserializing equipmentReservationSlot plain properties"
                )
                return null
            }

            val equipmentId = fireMapEquipment["id"] as? String
            val equipment = FireEquipment.deserialize(equipmentId, fireMapEquipment)

            if (equipment == null) {
                Log.e(
                    "deserialization error",
                    "Error deserializing fireEquipment in equipmentReservationSlot properties"
                )
                return null
            }

            return FireEquipmentReservationSlot(
                id,
                startSlot,
                endSlot,
                date,
                equipment,
                selectedQuantity,
                playgroundReservationId,
                timestamp
            )
        }

        fun slotsFromNewReservation(
            newReservation: NewReservation,
            newReservationId: String,
            reservationEquipmentsById: Map<String, FireEquipment>
        ): List<FireEquipmentReservationSlot> {

            val equipmentReservationSlots = mutableListOf<FireEquipmentReservationSlot>()

            // Computing the duration useful to retrieve the number of slots needed for the reservation
            val durationInMinutes =
                Duration.between(newReservation.startTime, newReservation.endTime).toMinutes()

            val nowTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

            // Creating a FireEquipmentReservationSlot object for each slot
            // every 30 minutes and for each equipment type
            for (i in 0 until durationInMinutes step 30) {
                for (equipment in newReservation.selectedEquipments) {
                    equipmentReservationSlots.add(
                        FireEquipmentReservationSlot(
                            null,
                            newReservation.startTime.plusMinutes(i)
                                .format(DateTimeFormatter.ISO_DATE_TIME),
                            newReservation.startTime.plusMinutes(i + 30)
                                .format(DateTimeFormatter.ISO_DATE_TIME),
                            newReservation.startTime.toLocalDate()
                                .format(DateTimeFormatter.ISO_DATE),
                            reservationEquipmentsById[equipment.equipmentId]!!.clone(),
                            equipment.selectedQuantity.toLong(),
                            newReservationId,
                            nowTimestamp
                        )
                    )
                }
            }

            return equipmentReservationSlots
        }
    }
}
