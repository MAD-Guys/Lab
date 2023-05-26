package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.PlaygroundInfo

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
    /**
     * Convert the FirePlaygroundSport object into a DetailedPlaygroundSport entity
     */

    fun toDetailedPlaygroundSport(): DetailedPlaygroundSport {
        return DetailedPlaygroundSport(
            id,
            playgroundName,
            sport.id,
            sport.emoji,
            sport.name,
            sportCenter.id,
            sportCenter.name,
            sportCenter.address,
            pricePerHour.toFloat()
        )
    }

    /**
     * Convert the FirePlaygroundSport object into a PlaygroundInfo entity including the reviewList and the various ratings
     */
    fun toPlaygroundInfo(fireReviewList: List<FireReview>): PlaygroundInfo {
        val playgroundInfo =  PlaygroundInfo(
            id,
            playgroundName,
            sportCenter.id,
            sportCenter.name,
            sport.id,
            sport.name,
            sport.emoji,
            sportCenter.address,
            sportCenter.openingHours,
            sportCenter.closingHours,
            pricePerHour.toFloat()
        )
        val overallQualityRating = fireReviewList.map { it.qualityRating }.average().toFloat()
        val overallFacilitiesRating = fireReviewList.map { it.facilitiesRating }.average().toFloat()
        playgroundInfo.overallQualityRating = overallQualityRating
        playgroundInfo.overallFacilitiesRating = overallFacilitiesRating
        playgroundInfo.overallRating = (overallQualityRating + overallFacilitiesRating) / 2
        playgroundInfo.reviewList = fireReviewList.map { it.toReview() }
        return playgroundInfo
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
