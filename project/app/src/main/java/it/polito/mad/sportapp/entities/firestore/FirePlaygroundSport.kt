package it.polito.mad.sportapp.entities.firestore

import android.util.Log

data class FirePlaygroundSport(
    val id: String,
    val playgroundName: String,
    val pricePerHour: Double,
    val sport: FireSport,
    val sportCenter: FireSportCenter
) {
    /**
     * Serialize the FirePlaygroundSport object into a Map<String, Any> object
     * to send to the Firestore cloud database
     */
    fun serialize(): Map<String, Any?> {
        return mapOf(
            // no id included in serialization
            "playgroundName" to playgroundName,
            "pricePerHour" to pricePerHour,
            "sport" to sport.serialize(true),
            "sportCenter" to sportCenter.serialize(true)
        )
    }

    companion object {
        /**
         * Create a FirePlaygroundSport object from raw Map<String,Any> data coming from Firestore
         */
        fun deserialize(id: String, data: Map<String, Any>?): FirePlaygroundSport? {
            if (data == null) {
                // deserialization error
                Log.d(
                    "deserialization error",
                    "Error deserializing FirePlaygroundSport the data passed is null in FirePlaygroundSport.deserialize()"
                )
                return null
            }

            val playgroundName = data["playgroundName"] as? String
            val pricePerHour = data["pricePerHour"] as? Double
            val rawSport = data["sport"] as? Map<String, Any>
            val rawSportCenter = data["sportCenter"] as? Map<String, Any>
            val sportId = rawSport?.get("id") as? String
            val sportCenterId = rawSportCenter?.get("id") as? String

            if (playgroundName == null || pricePerHour == null || rawSport == null || rawSportCenter == null || sportId == null || sportCenterId == null) {
                // deserialization error
                Log.d(
                    "deserialization error",
                    "Error deserializing sport center in FirePlaygroundSport.deserialize()"
                )
                return null
            }
            val sport = FireSport.deserialize(sportId, rawSport)
            val sportCenter =
                FireSportCenter.deserialize(sportCenterId, rawSportCenter)


            return FirePlaygroundSport(
                id,
                playgroundName,
                pricePerHour,
                sport!!,
                sportCenter!!
            )

        }
    }
}
