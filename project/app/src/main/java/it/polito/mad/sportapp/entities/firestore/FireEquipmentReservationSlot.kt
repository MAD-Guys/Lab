package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import java.time.LocalDateTime

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
            "equipment" to equipment.serialize(),
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
                Log.d("deserialization error", "trying to deserialize a equipmentReservationSlot with null data in FireUser.deserialize()")
                return null
            }
            val fireMapEquipment  = (fireMap["equipment"] as? Map<String, Any>?)
            val equipmentId = fireMapEquipment?.get("id") as String?
            val startSlot = fireMap["startSlot"] as? String
            val endSlot = fireMap["endSlot"] as? String
            val equipment = FireEquipment.deserialize( equipmentId, fireMapEquipment)
            val selectedQuantity = fireMap["selectedQuantity"] as? Long
            val playgroundReservationId  = fireMap["playgroundReservationId"] as? String
            val timestamp = fireMap["timestamp"] as? String

            if( startSlot == null || endSlot == null || equipment == null || selectedQuantity == null || playgroundReservationId == null || timestamp == null){
                Log.d("deserialization error", "Error deserializing equipmentReservationSlot plain properties")
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
