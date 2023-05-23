package it.polito.mad.sportapp.entities.firestore

data class FireSportLevel(
    val sportId : String,
    val sportName: String,
    val sportLevel: FireLevel
)

enum class FireLevel (level: String) {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    EXPERT("expert"),
    PRO("pro")
}
