package it.polito.mad.sportapp.entities.firestore

import java.time.LocalDateTime

data class FirePlaygroundReservation(
    val id: String,
    val playgroundId: String,
    val user: FireUserForPlaygroundReservation,
    val participants: List<FireUserForPlaygroundReservation>,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val totalPrice: Double,
    val additionalRequests: String,
    val timestamp: LocalDateTime
)
