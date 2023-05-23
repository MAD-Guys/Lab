package it.polito.mad.sportapp.entities.firestore

data class FireUser(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val gender: Gender,
    val age: Long,
    val location: String,
    val imageURL: String,
    val bio: String,
    val sportLevels: List<FireSportLevel>
)


enum class Gender(gender: String) {
    MALE("male"),
    FEMALE("female"),
    OTHER("other")
}
