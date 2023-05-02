package it.polito.mad.sportapp.entities

data class DetailedPlaygroundSport(
    val playgroundId: Int,
    val playgroundName: String,
    val sportId: Int,
    val sportCenterName: String,
    val pricePerHour: Float,
    var available: Boolean = false
)