package it.polito.mad.sportapp.entities

data class DetailedPlaygroundSport(
    val playgroundId: Int,
    val playgroundName: String,
    val sportId: Int,
    val sportCenterName: String,
    val pricePerHour: Float,
    var available: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetailedPlaygroundSport

        if (playgroundId != other.playgroundId) return false

        return true
    }

    override fun hashCode(): Int {
        return playgroundId
    }
}