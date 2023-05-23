package it.polito.mad.sportapp.entities.firestore

data class PlaygroundSport(
    val id: String,
    val playgroundName: String,
    val pricePerHour: Double,
    val sport: Sport,
    val sportCenter: SportCenter
)
