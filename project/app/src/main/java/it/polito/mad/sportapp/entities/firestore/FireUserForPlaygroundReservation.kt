package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.User

data class FireUserForPlaygroundReservation(
    val id: String,
    val username: String
) {
    /**
     * Serialize FireUserForPlaygroundReservation document data to send it to
     * cloud firestore db
     */
    fun serialize(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "username" to username
        )
    }

    companion object {
        /**
         * Deserialize a Map<String,Any> coming from Firestore in a proper
         * FireUserForPlaygroundReservation object
         */
        fun deserialize(data: Map<String, Any>?): FireUserForPlaygroundReservation? {
            if (data == null) {
                Log.e(
                    "deserialization error",
                    "Error deserializing FireUserForPlaygroundReservation, the data passed is null in FireUserForPlaygroundReservation.deserialize()"
                )
                return null
            }

            val id = data["id"] as? String
            val username = data["username"] as? String

            if (id == null || username == null) {
                Log.e(
                    "deserialization error",
                    "Error deserializing user properties in FireUserForPlaygroundReservation.deserialize()"
                )
                return null
            }

            return FireUserForPlaygroundReservation(
                id,
                username
            )
        }

        /** Create a FireUserForPlaygroundReservation from a User entity */
        fun from(user: User): FireUserForPlaygroundReservation {
            return FireUserForPlaygroundReservation(
                user.id!!,
                user.username
            )
        }
    }
}
