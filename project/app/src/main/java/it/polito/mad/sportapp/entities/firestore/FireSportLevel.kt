package it.polito.mad.sportapp.entities.firestore

data class FireSportLevel(
    val sportId : String,
    val sportName: String,
    val sportLevel: Level
)

enum class Level (level: String) {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    EXPERT("expert"),
    PRO("pro")
}
