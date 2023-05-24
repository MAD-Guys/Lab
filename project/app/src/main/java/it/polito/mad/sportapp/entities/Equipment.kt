package it.polito.mad.sportapp.entities

data class Equipment (
    val id: String,
    val name: String,
    val sportId: String,
    val sportCenterId: String,
    val unitPrice: Float,
    var availability: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Equipment

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun clone() = Equipment(
        id,
        name,
        sportId,
        sportCenterId,
        unitPrice,
        availability
    )
}