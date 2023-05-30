package it.polito.mad.sportapp.entities.firestore

import android.util.Log

data class FireSportCenter(
    val id: String,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val openingHours: String,
    val closingHours: String
) {
    /**
     * Serialize a FireSportCenter in a raw Map<String,Any> to store it in Firestore cloud db
     */
    fun serialize(withId: Boolean = false): Map<String, Any> {
        return if (withId) {
            mapOf(
                "id" to id,
                "name" to name,
                "address" to address,
                "phoneNumber" to phoneNumber,
                "openingHours" to openingHours,
                "closingHours" to closingHours
            )
        } else {
            mapOf(
                "name" to name,
                "address" to address,
                "phoneNumber" to phoneNumber,
                "openingHours" to openingHours,
                "closingHours" to closingHours
            )
        }
    }

    fun clone(): FireSportCenter {
        return FireSportCenter(
            id,
            name,
            address,
            phoneNumber,
            openingHours,
            closingHours
        )
    }

    companion object {
        /**
         * Create a FireSportCenter object from raw Map<String,Any> data coming from Firestore
         */
        fun deserialize(id: String?, rawSportCenter: Map<String, Any>?): FireSportCenter? {
            if(id == null) {
                Log.d(
                    "deserialization error",
                    "Error deserializing sport center the id passed is null in FireSportCenter.deserialize()"
                )
                return null
            }

            if (rawSportCenter == null) {
                Log.d(
                    "deserialization error",
                    "Error deserializing sport center the data passed is null in FireSportCenter.deserialize()"
                )
                return null
            }

            val name = rawSportCenter["name"] as? String
            val address = rawSportCenter["address"] as? String
            val phoneNumber = rawSportCenter["phoneNumber"] as? String
            val openingHours = rawSportCenter["openingHours"] as? String
            val closingHours = rawSportCenter["closingHours"] as? String

            if (name == null || address == null || phoneNumber == null || openingHours == null || closingHours == null) {
                // deserialization error
                Log.d(
                    "deserialization error",
                    "Error deserializing sport center properties in FireSportCenter.deserialize()"
                )
                return null
            }

            return FireSportCenter(
                id,
                name,
                address,
                phoneNumber,
                openingHours,
                closingHours
            )
        }
    }
}
