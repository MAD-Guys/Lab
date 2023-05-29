package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.Equipment

data class FireEquipment(
    val id: String,
    val name: String,
    val sportId: String,
    val sportCenterId: String,
    val unitPrice: Double,
    val maxQuantity: Long
) {

    /**
     * Serialize the FireEquipment object into a Map<String, Any> object
     * to send to the Firestore cloud database
     */
    fun serialize(withId: Boolean = false): Map<String, Any> {
        return if (withId) {
            mapOf(
                "id" to id,
                "name" to name,
                "sportId" to sportId,
                "sportCenterId" to sportCenterId,
                "unitPrice" to String.format("%f", unitPrice),
                "maxQuantity" to maxQuantity
            )
        }
        else {
            mapOf(
                // no id included in serialization
                "name" to name,
                "sportId" to sportId,
                "sportCenterId" to sportCenterId,
                "unitPrice" to String.format("%f", unitPrice),
                "maxQuantity" to maxQuantity
            )
        }
    }

    /**
     * Convert the FireUser object into a User entity
     */
    fun toEquipment(): Equipment {
        return Equipment(
            id,
            name,
            sportId,
            sportCenterId,
            unitPrice.toFloat(),
            maxQuantity.toInt()
        )
    }

    fun clone(): FireEquipment {
        return FireEquipment(
            id,
            name,
            sportId,
            sportCenterId,
            unitPrice,
            maxQuantity
        )
    }

    companion object {
        /**
         * Convert an Equipment object into a FireEquipment object
         */

        fun from(equipment: Equipment): FireEquipment {
            return FireEquipment(
                equipment.id,
                equipment.name,
                equipment.sportId,
                equipment.sportCenterId,
                equipment.unitPrice.toDouble(),
                equipment.availability.toLong()
            )
        }


        /**
         * Deserialize a Map<String,Any> coming from Firestore in a proper FireUser object;
         * it returns 'null' if the deserialization fails
         */
        fun deserialize(id: String?, fireMap: Map<String, Any>?): FireEquipment? {
            if (id == null) {
                // deserialization error
                Log.d("deserialization error", "trying to deserialize a equipment with null id in FireEquipment.deserialize()")
                return null
            }

            if (fireMap == null) {
                // deserialization error
                Log.d("deserialization error", "trying to deserialize a equipment with null data in FireEquipment.deserialize()")
                return null
            }

            val name = fireMap["name"] as? String
            val sportId = fireMap["sportId"] as? String
            val sportCenterId = fireMap["sportCenterId"] as? String
            val unitPrice = fireMap["unitPrice"] as? String
            val maxQuantity = fireMap["maxQuantity"] as? Long

            if (name == null || sportId == null || sportCenterId == null || unitPrice == null || maxQuantity == null) {
                // deserialization error
                Log.d("deserialization error", "Error deserializing equipment plain properties")
                return null
            }

            return FireEquipment(
                id,
                name,
                sportId,
                sportCenterId,
                unitPrice.toDouble(),
                maxQuantity
            )
        }



    }

}
