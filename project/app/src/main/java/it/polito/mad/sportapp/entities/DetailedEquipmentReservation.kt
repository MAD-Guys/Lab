package it.polito.mad.sportapp.entities

data class DetailedEquipmentReservation (
    val playgroundReservationId: String?,
    val equipmentId: String,
    val equipmentName: String,
    var selectedQuantity: Int,
    val unitPrice: Float,
    val totalPrice: Float
)