package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation

data class FireEquipmentReservationSlot(
    val id: String?,
    val startSlot: String,
    val endSlot: String,
    val equipment: FireEquipment,
    val selectedQuantity: Long,
    val playgroundReservationId: String,
    val timestamp: String
)
{
    /**
     * Serialize the FireEquipmentReservationSlot object into a Map<String, Any> object
     * to send to the Firestore cloud database
     */
    fun serialize(): Map<String, Any?> {
        return mapOf(
            // no id included in serialization
            "startSlot" to startSlot,
            "endSlot" to endSlot,
            "equipment" to equipment.serialize(withId=true),
            "selectedQuantity" to selectedQuantity,
            "playgroundReservationId" to playgroundReservationId,
            "timestamp" to timestamp
        )
    }
    /**
     * This method converts a FireEquipmentReservationSlot object into a DetailedEquipmentReservation entity
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
         * This method deserializes a Map<String, Any> object coming from Firestore DB into
         * a FireEquipmentReservationSlot object; it returns 'null' if the deserialization fails
         */
        fun deserialize(id: String, fireMap: Map<String, Any>?): FireEquipmentReservationSlot? {
            if (fireMap == null) {
                // deserialization error
                Log.d("deserialization error", "trying to deserialize a equipmentReservationSlot with null data in FireEquipmentReservationSlot.deserialize()")
                return null
            }

            val startSlot = fireMap["startSlot"] as? String
            val endSlot = fireMap["endSlot"] as? String
            @Suppress("UNCHECKED_CAST")
            val fireMapEquipment  = fireMap["equipment"] as? Map<String, Any>
            val selectedQuantity = fireMap["selectedQuantity"] as? Long
            val playgroundReservationId  = fireMap["playgroundReservationId"] as? String
            val timestamp = fireMap["timestamp"] as? String

            if(startSlot == null || endSlot == null || fireMapEquipment == null ||
                selectedQuantity == null || playgroundReservationId == null || timestamp == null){
                Log.d("deserialization error", "Error deserializing equipmentReservationSlot plain properties")
                return null
            }

            val equipmentId = fireMapEquipment["id"] as? String
            val equipment = FireEquipment.deserialize(equipmentId, fireMapEquipment)

            if (equipment == null) {
                Log.d("deserialization error", "Error deserializing fireEquipment in equipmentReservationSlot properties")
                return null
            }

            return FireEquipmentReservationSlot(
                id,
                startSlot,
                endSlot,
                equipment,
                selectedQuantity,
                playgroundReservationId,
                timestamp
            )
        }
    }
}
