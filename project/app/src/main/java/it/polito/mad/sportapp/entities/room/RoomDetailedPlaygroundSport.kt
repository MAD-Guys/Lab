package it.polito.mad.sportapp.entities.room

data class RoomDetailedPlaygroundSport(
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

        other as RoomDetailedPlaygroundSport

        if (playgroundId != other.playgroundId) return false

        return true
    }

    override fun hashCode(): Int {
        return playgroundId
    }

    fun exactlyEqualTo(playground: RoomDetailedPlaygroundSport): Boolean {
        return this.playgroundId == playground.playgroundId &&
                this.playgroundName == playground.playgroundName &&
                this.sportId == playground.sportId &&
                this.sportCenterName == playground.sportCenterName &&
                this.pricePerHour == playground.pricePerHour &&
                this.available == playground.available
    }

    fun clone() = RoomDetailedPlaygroundSport(
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