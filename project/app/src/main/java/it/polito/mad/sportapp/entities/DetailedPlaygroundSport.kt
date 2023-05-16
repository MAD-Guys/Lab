package it.polito.mad.sportapp.entities

data class DetailedPlaygroundSport(
    val playgroundId: Int,
    val playgroundName: String,
    val sportId: Int,
    val sportEmoji: String,
    val sportName: String,
    val sportCenterId: Int,
    val sportCenterName: String,
    val sportCenterAddress: String,
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

    fun exactlyEqualTo(playground: DetailedPlaygroundSport): Boolean {
        return this.playgroundId == playground.playgroundId &&
                this.playgroundName == playground.playgroundName &&
                this.sportId == playground.sportId &&
                this.sportCenterName == playground.sportCenterName &&
                this.pricePerHour == playground.pricePerHour &&
                this.available == playground.available
    }

    fun clone() = DetailedPlaygroundSport(
        playgroundId,
        playgroundName,
        sportId,
        sportEmoji,
        sportName,
        sportCenterId,
        sportCenterName,
        sportCenterAddress,
        pricePerHour,
        available
    )
}