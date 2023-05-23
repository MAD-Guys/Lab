package it.polito.mad.sportapp.entities.firestore

import java.time.LocalDateTime

data class FireNotification(
    val type: String,
    val reservationId: String,
    val senderId: String,
    val receiverId: String,
    val status: Status,
    val description: String,
    val timestamp: LocalDateTime,
)

enum class Status (status: String){
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    PENDING("Pending")
}
