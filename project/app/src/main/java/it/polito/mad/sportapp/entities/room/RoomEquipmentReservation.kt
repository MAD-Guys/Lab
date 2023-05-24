package it.polito.mad.sportapp.entities.room

import androidx.room.*

@Entity(
    tableName = "equipment_reservation",
    foreignKeys = [ForeignKey(
        entity = RoomPlaygroundReservation::class,
        parentColumns = ["id"],
        childColumns = ["playground_reservation_id"]
    ),
        ForeignKey(
            entity = RoomEquipment::class,
            parentColumns = ["id"],
            childColumns = ["equipment_id"]
        )]
)
data class RoomEquipmentReservation(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "playground_reservation_id", index = true)
    val playgroundReservationId: Int,
    @ColumnInfo(name = "equipment_id", index = true)
    val equipmentId: Int,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    @ColumnInfo(name = "timestamp")
    val timestamp: String,
    @ColumnInfo(name = "total_price")
    val totalPrice: Float,
)
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomEquipmentReservation

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }


}
