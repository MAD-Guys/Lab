package it.polito.mad.sportapp.entities

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Notification (
    val id: String?,
    val type: String,
    val reservationId: String,
    val senderUid: String,
    val receiverUid: String,
    val profileUrl: String?,     // TODO
    val status: NotificationStatus,
    val description: String,
    val timestamp: String,
) {
    var publicationDate: LocalDate = LocalDateTime.parse(timestamp).toLocalDate()
    var publicationTime: LocalTime = LocalDateTime.parse(timestamp).toLocalTime()
}