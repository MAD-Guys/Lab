package it.polito.mad.sportapp.entities.firestore

data class FirePlaygroundSport(
    val id: String,
    val playgroundName: String,
    val pricePerHour: Double,
    val sport: FireSport,
    val sportCenter: FireSportCenter
)
