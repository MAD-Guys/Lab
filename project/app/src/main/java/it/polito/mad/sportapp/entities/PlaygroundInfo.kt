package it.polito.mad.sportapp.entities

import java.time.LocalTime

data class PlaygroundInfo (
    val playgroundId: String,
    val playgroundName: String,
    val sportCenterId: String,
    val sportCenterName: String,
    val sportId: String,
    val sportName: String,
    val sportEmoji: String,
    val sportCenterAddress: String,
    val openingTime: String,
    val closingTime: String,
    val pricePerHour: Float
) {
    var overallQualityRating: Float = 0f
    var overallFacilitiesRating: Float = 0f
    val openingHours: LocalTime = LocalTime.parse(openingTime)
    val closingHours: LocalTime = LocalTime.parse(closingTime)
    var overallRating: Float = (overallQualityRating + overallFacilitiesRating) / 2
    var reviewList: List<Review> = listOf()
}