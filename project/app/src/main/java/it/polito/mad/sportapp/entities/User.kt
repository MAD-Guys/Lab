package it.polito.mad.sportapp.entities

import it.polito.mad.sportapp.entities.Achievement.*


data class User (
    val id: String?,
    val firstName: String,
    val lastName: String,
    val username: String,
    val gender: String,
    val age: Int,
    val location: String,
    val imageURL: String?,  // TODO
    val bio: String,
    val notificationsToken: String? = null  // TODO
) {
    var sportLevels: List<SportLevel> = mutableListOf()

    var achievements : Map<Achievement,Boolean> = mapOf(
        AtLeastOneSport to false,
        AtLeastFiveSports to false,
        AllSports to false,
        AtLeastThreeMatches to false,
        AtLeastTenMatches to false,
        AtLeastTwentyFiveMatches to false,
    )

    fun clone(): User {
        return User(
            id,
            firstName,
            lastName,
            username,
            gender,
            age,
            location,
            imageURL,
            bio,
            notificationsToken
        ).also { clonedUser ->
            // clone and add sport levels
            val clonedSportLevels = mutableListOf<SportLevel>()
            clonedSportLevels.addAll(this.sportLevels.map { it.clone() })
            clonedUser.sportLevels = clonedSportLevels

            // clone and add achievements
            val clonedAchievements = mutableMapOf<Achievement,Boolean>()
            this.achievements.forEach { (achievement, bool) -> clonedAchievements[achievement] = bool }
            clonedUser.achievements = clonedAchievements
        }
    }
}