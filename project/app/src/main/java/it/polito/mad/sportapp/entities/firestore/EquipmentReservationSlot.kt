package it.polito.mad.sportapp.entities.firestore

import java.time.LocalDateTime

data class EquipmentReservationSlot(
    val id: String,
    val startSlot: LocalDateTime,
    val endSlot: LocalDateTime,
    val equipment: Equipment,
    val selectedQuantity: Long,
    val playgroundReservationId: String,
    val timestamp: LocalDateTime
)
