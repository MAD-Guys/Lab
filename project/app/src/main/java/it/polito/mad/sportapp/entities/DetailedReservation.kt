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
    val totalPrice: Float,
) {
    var date: LocalDate = LocalDate.parse(startDateTime.substring(0, 10))
    var startTime: LocalTime = LocalTime.parse(startDateTime.substring(11, 19))
    var endTime: LocalTime = LocalTime.parse(endDateTime.substring(11, 19))
    var startLocalDateTime: LocalDateTime = LocalDateTime.parse(startDateTime)
    var endLocalDateTime: LocalDateTime = LocalDateTime.parse(endDateTime)
    var equipments: MutableList<DetailedEquipmentReservation> = mutableListOf()

    val duration = Duration.between(
        LocalTime.parse(startDateTime.substring(11, 19)), LocalTime.parse(
            endDateTime.substring(11, 19)
        )
    ).toMinutes()

    var endSlot: LocalDateTime = endLocalDateTime.minusMinutes(duration)
    var startSlot: LocalDateTime = startLocalDateTime

    fun printSportNameWithEmoji(emojiOnTheLeft: Boolean = false): String {
        return if (emojiOnTheLeft) "$sportEmoji $sportName" else "$sportName $sportEmoji"
    }
}