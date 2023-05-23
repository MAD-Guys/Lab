package it.polito.mad.sportapp.entities.firestore

import it.polito.mad.sportapp.entities.User

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
) {
    /**
     * This method serializes the FireUser object into a HashMap<String, Any> object to send to Firestore database
     */
    fun serialize(): Map<String, Any>? {
        val map: Map<String, Any>? = mapOf(
            "uid" to uid,
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username,
            "gender" to gender.name,
            "age" to age,
            "location" to location,
            "imageURL" to imageURL,
            "bio" to bio,
            "sportLevels" to sportLevels.map {
                mapOf(
                    "sportId" to it.sportId,
                    "sportName" to it.sportName,
                    "level" to it.sportLevel
                )
            }
        )

        return map
    }
    /**
     * This method converts the FireUser object into a User object
     */
    fun to() : User{
        return User(
            1,//TODO: id
            firstName,
            lastName,
            username,
            gender.name,
            age.toInt(),
            location,
            bio
        )
    }

    companion object {
        /**
         * This method converts a User object into a FireUser object
         */
        fun from(user: User): FireUser {

            return FireUser(
                user.id.toString(),//TODO: uid
                user.firstName,
                user.lastName,
                user.username,
                Gender.valueOf(user.gender),
                user.age.toLong(),
                user.location,
                "", //TODO: imageURL
                user.bio,
                user.sportLevel.map {
                    FireSportLevel(
                        it.sportId.toString(),//TODO: sportId
                        it.sport!!,
                        FireLevel.valueOf(it.level!!)
                    )
                },
            )
        }

        /**
         * This method deserializes a HashMap<String, Any> object coming from Firestore DB into a FireUser object
         */
        fun deserialize(map: Map<String, Any>): FireUser {
            return FireUser(
                map["uid"] as String,
                map["firstName"] as String,
                map["lastName"] as String,
                map["username"] as String,
                Gender.valueOf(map["gender"] as String),
                map["age"] as Long,
                map["location"] as String,
                map["imageURL"] as String,
                map["bio"] as String,
                (map["sportLevels"] as List<HashMap<String, Any>>).map {
                    FireSportLevel(
                        it["sportId"] as String,
                        it["sportName"] as String,
                        FireLevel.valueOf(it["level"] as String)
                    )
                }
            )
        }
    }
}


enum class Gender(gender: String) {
    MALE("male"),
    FEMALE("female"),
    OTHER("other")
}
