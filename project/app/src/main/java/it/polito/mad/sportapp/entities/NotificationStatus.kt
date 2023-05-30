package it.polito.mad.sportapp.entities

enum class NotificationStatus {
    ACCEPTED, CANCELED, PENDING, REJECTED;

    val index = ordinal.toLong()

    companion object {
        fun from(name: String) = when (name) {
            "ACCEPTED" -> ACCEPTED
            "CANCELED" -> CANCELED
            "PENDING" -> PENDING
            "REJECTED" -> REJECTED
            else -> throw RuntimeException("It does not exist a NotificationStatus from $name")
        }
    }
}