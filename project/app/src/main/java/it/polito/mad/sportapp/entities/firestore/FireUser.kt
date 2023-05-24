package it.polito.mad.sportapp.entities.firestore

import android.util.Log
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.firestore.FireSportLevel.*


enum class Gender(val gender: String) {
    Male("male"),
    Female("female"),
    Other("other");

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
    val id: String?,
    val firstName: String,
    val lastName: String,
    val username: String,
    val gender: Gender,
    val age: Long,
    val location: String,
    val imageURL: String?,
    val bio: String,
    val sportLevels: List<FireSportLevel>
) {
    /**
     * Serialize the FireUser object into a Map<String, Any> object
     * to send to the Firestore cloud database
     */
    fun serialize(): Map<String, Any?> {
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
     * Convert the FireUser object into a User entity
     */
    fun toUser() : User {
        val entity = User(
            id,
            firstName,
            lastName,
            username,
            gender.gender.substring(0,1).uppercase() + gender.gender.substring(1),
            age.toInt(),
            location,
            imageURL,
            bio
        )

        // add user sport levels
        entity.sportLevels = sportLevels.map { it.toSportLevel() }

        return entity
    }

    companion object {
        /**
         * Convert a User object into a FireUser object
         */
        fun from(user: User): FireUser {

            return FireUser(
                user.id,
                user.firstName,
                user.lastName,
                user.username,
                Gender.valueOf(user.gender),
                user.age.toLong(),
                user.location,
                user.imageURL,
                user.bio,
                user.sportLevels.map {
                    FireSportLevel(
                        it.sportId,
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
            val username = fireMap["username"] as? String
            val gender = Gender.of(fireMap["gender"] as? Long)
            val age = fireMap["age"] as? Long
            val location = fireMap["location"] as? String
            val imageURL = fireMap["imageURL"] as? String?
            val bio = fireMap["bio"] as? String
            val rawSportLevels = fireMap["sportLevels"] as? List<*>

            if (firstName == null || lastName == null || username == null || gender == null ||
                age == null || location == null || bio == null ||
                rawSportLevels == null) {
                // deserialization error
                Log.d("deserialization error", "Error deserializing user plain properties")
                return null
            }

            val sportLevels = rawSportLevels.map {
                @Suppress("UNCHECKED_CAST")
                val rawSportLevel = it as? Map<String, Any>
                val deserializedSportLevel = FireSportLevel.deserialize(rawSportLevel) ?: return null

                deserializedSportLevel
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



