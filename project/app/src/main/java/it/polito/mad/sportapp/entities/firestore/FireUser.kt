package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.FireSportLevel.*


enum class Gender(val gender: String) {
    MALE("male"),
    FEMALE("female"),
    OTHER("other");

    val index = this.ordinal.toLong()

    companion object {
        fun of(rawGender: Long?): Gender? {
            if(rawGender == null || rawGender > Gender.values().size)
                return null

            return Gender.values()[rawGender.toInt()]
        }
    }
}

data class FireUser(
    val id: String,
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
     * Serialize the FireUser object into a Map<String, Any> object
     * to send to the Firestore cloud database
     */
    fun serialize(): Map<String, Any> {
        return mapOf(
            // no id included in serialization
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username,
            "gender" to gender.index,
            "age" to age,
            "location" to location,
            "imageURL" to imageURL,
            "bio" to bio,
            "sportLevels" to sportLevels.map { it.serialize() }
        )
    }

    /**
     * Convert the FireUser object into a User object
     */
    fun toUser() : User{
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
         * Convert a User object into a FireUser object
         */
        fun from(user: User): FireUser {

            return FireUser(
                user.id.toString(), // TODO: uid
                user.firstName,
                user.lastName,
                user.username,
                Gender.valueOf(user.gender),
                user.age.toLong(),
                user.location,
                "",     // TODO: imageURL
                user.bio,
                user.sportLevel.map {
                    FireSportLevel(
                        it.sportId.toString(),  // TODO: sportId
                        it.sport!!,
                        Level.valueOf(it.level!!)
                    )
                },
            )
        }

        /**
         * This method deserializes a Map<String, Any> object coming from Firestore DB into
         * a FireUser object; it returns 'null' if the deserialization fails
         */
        fun deserialize(id: String, fireMap: Map<String, Any>?): FireUser? {
            if (fireMap == null) {
                // deserialization error
                Log.d("deserialization error", "trying to deserialize a user with null data in FireUser.deserialize()")
                return null
            }

            val firstName = fireMap["firstName"] as? String
            val lastName = fireMap["lastName"] as? String
            val username = fireMap["userName"] as? String
            val gender = Gender.of(fireMap["gender"] as? Long)
            val age = fireMap["age"] as? Long
            val location = fireMap["location"] as? String
            val imageURL = fireMap["imageURL"] as? String
            val bio = fireMap["bio"] as? String
            val rawSportLevels = fireMap["sportLevels"] as? List<*>

            if (firstName == null || lastName == null || username == null || gender == null ||
                age == null || location == null || imageURL == null || bio == null ||
                rawSportLevels == null) {
                // deserialization error
                Log.d("deserialization error", "Error deserializing user plain properties")
                return null
            }

            val sportLevels = rawSportLevels.map {
                val rawSportLevel = it as? Map<*, *>? ?: return null    // deserialization error

                val sportId = rawSportLevel["sportId"] as? String
                val sportName = rawSportLevel["sportName"] as? String
                val level = Level.of(rawSportLevel["level"] as? Long)

                if (sportId == null || sportName == null || level == null) {
                    // deserialization error
                    Log.d("deserialization error", "Error deserializing user sport level")
                    return null
                }

                FireSportLevel(sportId, sportName, level)
            }

            return FireUser(
                id,
                firstName,
                lastName,
                username,
                gender,
                age,
                location,
                imageURL,
                bio,
                sportLevels
            )
        }
    }
}



