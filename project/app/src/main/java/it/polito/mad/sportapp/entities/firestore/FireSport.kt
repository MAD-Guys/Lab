package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.room.RoomSport

data class FireSport(
    val id: String,
    val name: String,
    val emoji: String,
    val maxParticipants: Long
) {
    /** convert Firestore sport document to ProfileSport entity */
    fun toSport(): Sport {
        return Sport(
            id,
            name,
            emoji,
            maxParticipants.toInt()
        )
    }

    /** Serialize sport document data to send it to cloud firestore db */
    fun serialize(withId: Boolean = false): Map<String, Any> {
        return if (withId) {
            mapOf(
                "id" to id,
                "name" to name,
                "emoji" to emoji,
                "maxParticipants" to maxParticipants
            )
        } else {
            mapOf(
                // no id included in serialization
                "name" to name,
                "emoji" to emoji,
                "maxParticipants" to maxParticipants
            )
        }
    }

    fun clone(): FireSport {
        return FireSport(
            id,
            name,
            emoji,
            maxParticipants
        )
    }


    companion object {
        /**
         * Deserialize a Map<String,Any> coming from Firestore in a proper
         * FireSport object
         */
        fun deserialize(id: String?, data: Map<String, Any>?): FireSport? {
            if (id == null) {
                // deserialization error
                Log.e(
                    "deserialization error",
                    "trying to deserialize a sport with null id in FireSport.deserialize()"
                )
                return null
            }

            if (data == null) {
                // deserialization error
                Log.e(
                    "deserialization error",
                    "trying to deserialize a sport with null data in FireSport.deserialize()"
                )
                return null
            }

            val name = data["name"] as? String
            val emoji = data["emoji"] as? String
            val maxParticipants = data["maxParticipants"] as? Long

            if (name == null || emoji == null || maxParticipants == null) {
                // deserialization error
                Log.e(
                    "deserialization error",
                    "trying to deserialize a sport with null properties in FireSport.deserialize()"
                )
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
         * Convert a ProfileSport entity in a FireSport object compliant to Firestore
         * collection
         */
        fun from(sport: RoomSport): FireSport {
            return FireSport(
                sport.id.toString(), // TODO
                sport.name,
                sport.emoji,
                sport.maxPlayers.toLong()
            )
        }
    }
}
