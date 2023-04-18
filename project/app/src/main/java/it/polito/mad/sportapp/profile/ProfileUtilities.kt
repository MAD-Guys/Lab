package it.polito.mad.sportapp.profile

import com.google.android.material.chip.Chip
import it.polito.mad.sportapp.R
import org.json.JSONObject

/* Sport utilities */

internal data class Sport(val name: String, var selected: Boolean, var level: Level) {
    companion object {
        fun from(name: String, jsonObject: JSONObject): Sport =
            jsonObject.getJSONObject(name).let {
                Sport(name, it.getBoolean("selected"), Level.of(it.getInt("level")))
            }
    }

    fun saveAsJson(jsonObject: JSONObject) {
        val sportJson = JSONObject()
        sportJson.put("selected", this.selected)
        sportJson.put("level", this.level.ordinal)  // "level" -> 0/1/2/3/4
        jsonObject.put(this.name, sportJson)
    }
}

internal fun extendedNameOf(sportName: String): String = when(sportName) {
    "basket" -> "Basket"
    "soccer5" -> "5-a-side Soccer"
    "soccer8" -> "8-a-side Soccer"
    "soccer11" -> "11-a-side Soccer"
    "tennis" -> "Tennis"
    "tableTennis" -> "Table Tennis"
    "volleyball" -> "Volleyball"
    "beachVolley" -> "Beach Volley"
    "padel" -> "Padel"
    "miniGolf" -> "Mini Golf"
    "swimming" -> "Swimming"
    else -> "????"
}

internal fun getHardcodedSports() = arrayOf(
    Sport("basket", true, Level.EXPERT),
    Sport("tennis", true, Level.BEGINNER)
)

internal class SportChips(
    val name: String, val chip: Chip, val actualLevelChip: Chip
)

/* Enum utilities */

internal enum class Gender {
    Male, Female, Other
}

internal enum class Level {
    BEGINNER, INTERMEDIATE, EXPERT, PRO, NO_LEVEL;

    companion object {
        fun of(ordinal: Int): Level = when(ordinal) {
            0 -> BEGINNER
            1 -> INTERMEDIATE
            2 -> EXPERT
            3 -> PRO
            4 -> NO_LEVEL
            else -> throw RuntimeException("It does not exist a Level of $ordinal")
        }
    }

    fun icon() = when(this) {
        BEGINNER -> R.drawable.beginner_level_badge
        INTERMEDIATE -> R.drawable.intermediate_level_badge
        EXPERT -> R.drawable.expert_level_badge
        PRO -> R.drawable.pro_level_badge
        else -> throw RuntimeException("It does not exist such level")
    }
}