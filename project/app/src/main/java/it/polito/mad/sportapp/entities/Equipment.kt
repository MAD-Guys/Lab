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
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "sport_id", index = true)
    val sportId: Int,
    @ColumnInfo(name = "sport_center_id", index = true)
    val sportCenterId: Int,
    @ColumnInfo(name = "unit_price")
    val price: Float,
    @ColumnInfo(name = "availability")
    val availability: Int,
)



