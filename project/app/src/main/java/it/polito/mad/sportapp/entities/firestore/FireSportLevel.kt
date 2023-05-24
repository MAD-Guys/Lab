package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.SportLevel

data class FireSportLevel(
    val sportId : String,
    val sportName: String,
    val sportLevel: Level
) {
    companion object {
        fun deserialize(rawSportLevel: Map<String, Any>?): FireSportLevel? {
            if(rawSportLevel == null) {
                // deserialization error
                Log.d("deserialization error", "Error deserializing user sport level in FireSportLevel.deserialize()")
                return null
            }

            val sportId = rawSportLevel["sportId"] as? String
            val sportName = rawSportLevel["sportName"] as? String
            val sportLevel = Level.of(rawSportLevel["sportLevel"] as? Long)

            if (sportId == null || sportName == null || sportLevel == null) {
                // deserialization error
                Log.d("deserialization error", "Error deserializing user sport level")
                return null
            }

            return FireSportLevel(sportId, sportName, sportLevel)
        }
    }

    fun serialize(): Map<String, Any> {
        return mapOf(
            "sportId" to sportId,
            "sportName" to sportName,
            "sportLevel" to sportLevel.index
        )
    }

    /**
     * Convert firestore user sport level data to a proper SportLevel entity
     */
    fun toSportLevel(): SportLevel {
        return SportLevel(
            sportId,
            sportName,
            sportLevel.level.uppercase()
        )
    }


    enum class Level (val level: String) {
        BEGINNER("beginner"),
        INTERMEDIATE("intermediate"),
        EXPERT("expert"),
        PRO("pro");

        val index = this.ordinal.toLong()

        companion object {
            fun of(rawLevel: Long?): Level? {
                if(rawLevel == null || rawLevel > Level.values().size)
                    return null

                return Level.values()[rawLevel.toInt()]
            }
        }
    }
}


