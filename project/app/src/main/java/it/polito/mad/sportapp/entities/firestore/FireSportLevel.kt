package it.polito.mad.sportapp.entities.firestore

data class FireSportLevel(
    val sportId : String,
    val sportName: String,
    val sportLevel: Level
) {
    fun serialize(): Map<String, Any> {
        return mapOf(
            "sportId" to sportId,
            "sportName" to sportName,
            "sportLevel" to sportLevel.index
        )
    }


    enum class Level (val level: String) {
        BEGINNER("beginner"),
        INTERMEDIATE("intermediate"),
        EXPERT("expert"),
        PRO("pro");

        val index = this.ordinal.toLong()

        companion object {
            fun of(rawLevel: Long?): Level? {
                if(rawLevel == null || rawLevel > Level.values().size)
                    return null

                return Level.values()[rawLevel.toInt()]
            }
        }
    }
}


