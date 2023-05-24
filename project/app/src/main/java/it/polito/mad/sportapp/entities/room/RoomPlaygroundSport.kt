package it.polito.mad.sportapp.entities.room

import androidx.room.*

@Entity(
    tableName = "playground_sport",
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
data class RoomPlaygroundSport(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "sport_id", index = true)
    val sportId: Int,
    @ColumnInfo(name = "sport_center_id" , index = true)
    val sportCenterId: Int,
    @ColumnInfo(name = "playground_name" )
    val playgroundName: String,
    @ColumnInfo(name = "cost_per_hour")
    val pricePerHour: Float,
)




