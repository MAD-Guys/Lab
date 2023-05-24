package it.polito.mad.sportapp.entities

import java.time.LocalDateTime

data class Review (
    val id: String?,
    val userId: String,
    val playgroundId: String,
    val title: String,
    val qualityRating: Float,
    val facilitiesRating: Float,
    val review: String,
    var timestamp: String,
    var lastUpdate: String,
) {
    var publicationDate: LocalDateTime = LocalDateTime.parse(timestamp)
    var lastUpdateDate: LocalDateTime = LocalDateTime.parse(lastUpdate)
    var username : String = ""
}