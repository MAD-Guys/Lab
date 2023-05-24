package it.polito.mad.sportapp.entities

data class NewReservationEquipment (
    val equipmentId: String,
    val equipmentName: String,
    val selectedQuantity: Int,
    val unitPrice: Float
)