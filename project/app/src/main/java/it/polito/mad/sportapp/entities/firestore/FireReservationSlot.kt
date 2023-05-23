package it.polito.mad.sportapp.entities.firestore

import java.time.LocalDateTime

data class FireReservationSlot(
    val id: String,
    val startSlot: LocalDateTime,
    val endSlot: LocalDateTime,
    val playgroundId: String,
    val openPlaygroundsIds: List<String>,
    val reservationId: String
)
