package it.polito.mad.sportapp.entities.room

import androidx.room.*

@Entity(
    tableName = "equipment",
    foreignKeys = [ForeignKey(
        entity = RoomSport::class,
        parentColumns = ["id"],
        childColumns = ["sport_id"]
    ),
        ForeignKey(
            entity = RoomSportCenter::class,
            parentColumns = ["id"],
            childColumns = ["sport_center_id"]
        )]
)
data class RoomEquipment(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "sport_id", index = true)
    val sportId: Int,
    @ColumnInfo(name = "sport_center_id", index = true)
    val sportCenterId: Int,
    @ColumnInfo(name = "unit_price")
    val unitPrice: Float,
    @ColumnInfo(name = "availability")
    var availability: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomEquipment

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    fun clone() = RoomEquipment(
        id,
        name,
        sportId,
        sportCenterId,
        unitPrice,
        availability
    )
}



