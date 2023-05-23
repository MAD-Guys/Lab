package it.polito.mad.sportapp.entities.firestore

data class Equipment(
    val id: String,
    val name: String,
    val sportId: String,
    val sportCenterId: String,
    val unitPrice: Double,
    val maxQuantity: Long
)
