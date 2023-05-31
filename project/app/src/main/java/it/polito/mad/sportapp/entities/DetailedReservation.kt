package it.polito.mad.sportapp.entities

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class DetailedReservation (
    val id: String,
    val userId: String,
    val username: String,
    val sportCenterId: String,
    val sportId: String,
    val sportEmoji: String,
    val sportCenterName: String,
    val address: String,
    val sportName: String,
    val startDateTime: String,
    val endDateTime: String,
    val playgroundId: String,
    val playgroundName: String,
    val playgroundPricePerHour: Float,
    val additionalRequests: String?,
    val totalPrice: Float,
    val participants: List<String>   // usernames
) {
    var equipments: MutableList<DetailedEquipmentReservation> = mutableListOf()

    var date: LocalDate = LocalDate.parse(startDateTime.substring(0, 10))
    var startTime: LocalTime = LocalTime.parse(startDateTime.substring(11, 19))
    var endTime: LocalTime = LocalTime.parse(endDateTime.substring(11, 19))
    var startLocalDateTime: LocalDateTime = LocalDateTime.parse(startDateTime)
    var endLocalDateTime: LocalDateTime = LocalDateTime.parse(endDateTime)
    val duration = Duration.between(startTime, endTime).toMinutes()

    var startSlot: LocalDateTime = startLocalDateTime
    var endSlot: LocalDateTime = endLocalDateTime.minusMinutes(duration)

    fun printSportNameWithEmoji(emojiOnTheLeft: Boolean = false): String {
        return if (emojiOnTheLeft) "$sportEmoji $sportName" else "$sportName $sportEmoji"
    }
}