package it.polito.mad.sportapp.entities

enum class NotificationStatus {
    ACCEPTED, REJECTED, PENDING, CANCELED;

    companion object {
        fun from(name: String) = when (name) {
            "ACCEPTED" -> ACCEPTED
            "REJECTED" -> REJECTED
            "PENDING" -> PENDING
            "CANCELED" -> CANCELED
            else -> throw RuntimeException("It does not exist a NotificationStatus from $name")
        }
    }
}