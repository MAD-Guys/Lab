package it.polito.mad.sportapp.entities.firestore
import java.time.LocalDateTime

data class Review(
    val id: String,
    val uid: String,
    val playgroundId: String,
    val title: String,
    val qualityRating: Long,
    val facilitiesRating: Long,
    val textualReview: String,
    val timestamp: LocalDateTime,
    val lastUpdate: LocalDateTime
)
