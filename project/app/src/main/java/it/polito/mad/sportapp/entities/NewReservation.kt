package it.polito.mad.sportapp.entities

import java.time.LocalDateTime

/**
 * **id** has to be 'null' if this is a brand new reservation to store; not 'null'
 * if it is an existing reservation to be updated
 */
data class NewReservation (
    val id: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val playgroundId: String,
    val playgroundName: String,
    val playgroundPricePerHour: Float,
    val sportId: String,
    val sportEmoji: String,
    val sportName: String,
    val sportCenterId: String,
    val sportCenterName: String,
    val sportCenterAddress: String,
    val selectedEquipments: List<NewReservationEquipment>,
    val additionalRequests : String?
)