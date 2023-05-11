package it.polito.mad.sportapp.profile

import com.google.android.material.chip.Chip
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.SportLevel

/* Sport utilities */

internal data class Sport(val name: String, var selected: Boolean, var level: Level) {
    companion object {
        fun from(name: String, level: String): Sport =
            Sport(name, true, Level.valueOf(level.uppercase()))
    }

    fun toSportLevel(): SportLevel = SportLevel(0, name, level.name)
}

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