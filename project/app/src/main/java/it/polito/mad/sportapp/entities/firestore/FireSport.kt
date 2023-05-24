package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.room.RoomSport

data class FireSport(
    val id : String,
    val name: String,
    val emoji: String,
    val maxParticipants: Long
) {
    companion object {
        /**
         * Deserialize a Map<String,Any> coming from Firestore in a proper FireSport object
         */
        fun deserialize(id: String, data: Map<String,Any>?): FireSport? {
            if (data == null) {
                // deserialization error
                Log.d("deserialization error", "trying to deserialize a sport with null data in FireSport.deserialize()")
                return null
            }

            val name = data["name"] as? String
            val emoji = data["emoji"] as? String
            val maxParticipants = data["maxParticipants"] as? Long

            if (name == null || emoji == null || maxParticipants == null) {
                // deserialization error
                Log.d("deserialization error", "trying to deserialize a sport with null properties in FireSport.deserialize()")
                return null
            }

            return FireSport(
                id,
                name,
                emoji,
                maxParticipants
            )
        }

        /**
         * Convert a Sport entity in a FireSport object compliant to Firestore collection
         */
        fun from(sport: RoomSport) : FireSport {
            return FireSport(
                sport.id.toString(), // TODO
                sport.name,
                sport.emoji,
                sport.maxPlayers.toLong()
            )
        }
    }

}
