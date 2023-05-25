package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.Review
import java.time.format.DateTimeParseException

data class FireReview(
    val id: String?,
    val userId: String,
    val playgroundId: String,
    val title: String,
    val qualityRating: Long,
    val facilitiesRating: Long,
    val textualReview: String,
    val timestamp: String,  // TODO: note: I would prefer to use ISO string here, rather than Firebase Timestamp
    val lastUpdate: String  // TODO: note: I would prefer to use ISO string here, rather than Firebase Timestamp
) {
    /**
     * Serialize a FireReview in a raw Map<String,Any> to store it in Firestore cloud db
     */
    fun serialize(): Map<String,Any> {
        return mapOf(
            // no id in serialization
            "userId" to userId,
            "playgroundId" to playgroundId,
            "title" to title,
            "qualityRating" to qualityRating,
            "facilitiesRating" to facilitiesRating,
            "textualReview" to textualReview,
            "timestamp" to timestamp,
            "lastUpdate" to lastUpdate
        )
    }

    /**
     * Convert a FireReview document object to a Review entity
     */
    fun toReview(): Review? {
        try {
            return Review(
                id,
                userId,
                playgroundId,
                title,
                qualityRating.toFloat(),
                facilitiesRating.toFloat(),
                textualReview,
                timestamp,
                lastUpdate
            )
        }
        catch (e: DateTimeParseException) {
            Log.d("deserialization error", "Error: an error occurred parsing fireReview dates in FireReview.toReview()")
            return null
        }
    }

    companion object {
        /**
         * Create a FireReview object from raw Map<String,Any> data coming from Firestore
         */
        fun deserialize(id: String, data: Map<String,Any>?): FireReview? {
            if (data == null) {
                Log.d("deserialization error", "Error: trying to deserialize review with null data in FireReview.deserialize()")
                return null
            }

            val userId = data["userId"] as? String
            val playgroundId = data["playgroundId"] as? String
            val title = data["title"] as? String
            val qualityRating = data["qualityRating"] as? Long
            val facilitiesRating = data["facilitiesRating"] as? Long
            val textualReview = data["textualReview"] as? String
            val timestamp = data["timestamp"] as? String
            val lastUpdate = data["timestamp"] as? String

            if(userId == null || playgroundId == null || title == null || qualityRating == null ||
                    facilitiesRating == null || textualReview == null || timestamp == null ||
                lastUpdate == null) {
                Log.d("deserialization error", "Error: an error occurred deserialize review plain properties in FireReview.deserialize()")
                return null
            }

            // * ok *
            return FireReview(
                id,
                userId,
                playgroundId,
                title,
                qualityRating,
                facilitiesRating,
                textualReview,
                timestamp,
                lastUpdate
            )
        }

        /**
         * Convert a Review entity to a FireReview document object
         */
        fun from(review: Review): FireReview {
            return FireReview(
                review.id,
                review.userId,
                review.playgroundId,
                review.title,
                review.qualityRating.toLong(),
                review.facilitiesRating.toLong(),
                review.review,
                review.timestamp,
                review.lastUpdate
            )
        }
    }
}
