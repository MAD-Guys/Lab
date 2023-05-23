package it.polito.mad.sportapp.entities.firestore

data class FireSport(
    val id : String,
    val name: String,
    val emoji: String,
    val maxParticipants: Long
)
