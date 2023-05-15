package it.polito.mad.sportapp.entities

import androidx.room.*

@Entity(
    tableName = "equipment",
    foreignKeys = [ForeignKey(
        entity = Sport::class,
        parentColumns = ["id"],
        childColumns = ["sport_id"]
    ),
        ForeignKey(
            entity = SportCenter::class,
            parentColumns = ["id"],
            childColumns = ["sport_center_id"]
        )]
)
data class Equipment(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "sport_id", index = true)
    val sportId: Int,
    @ColumnInfo(name = "sport_center_id", index = true)
    val sportCenterId: Int,
    @ColumnInfo(name = "unit_price")
    val price: Float,
    @ColumnInfo(name = "availability")
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
        return id
    }

    fun clone() = Equipment(
        id,
        name,
        sportId,
        sportCenterId,
        price,
        availability
    )
}



