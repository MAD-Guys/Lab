package it.polito.mad.sportapp.entities.firestore

import android.util.Log

data class FireUserForPlaygroundReservation(
    val uid: String,
    val username: String
){
    /**
     * Serialize FireUserForPlaygroundReservation document data to send it to cloud firestore db
     */
    fun serialize(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "username" to username
        )
    }

    companion object {
        /**
         * Deserialize a Map<String,Any> coming from Firestore in a proper FireUserForPlaygroundReservation object
         */
        fun deserialize(data: Map<String,Any>?): FireUserForPlaygroundReservation? {
            if (data == null) {
                Log.d(
                    "deserialization error",
                    "Error deserializing FireUserForPlaygroundReservation the data passed is null in FireUserForPlaygroundReservation.deserialize()"
                )
                return null
            }

            val uid = data["uid"] as? String
            val username = data["username"] as? String

            if (uid == null || username == null) {
                Log.d(
                    "deserialization error",
                    "Error deserializing sport center in FireSportCenter.deserialize()"
                )
                return null
            }

            return FireUserForPlaygroundReservation(
                uid,
                username
            )
        }
    }
}
