package it.polito.mad.sportapp.entities

data class SportLevel (
    val sportId: String,
    val sport: String?,
    val level: String?
) {

    fun clone(): SportLevel {
        return SportLevel(
            sportId,
            sport,
            level
        )
    }
}