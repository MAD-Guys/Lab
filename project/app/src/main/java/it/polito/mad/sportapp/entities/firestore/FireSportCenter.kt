package it.polito.mad.sportapp.entities.firestore

import java.time.LocalDateTime

data class FireSportCenter(
    val id: String,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val openingHours: LocalDateTime,
    val ClosingHours: LocalDateTime
)
