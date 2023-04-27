package it.polito.mad.sportapp.entities

import androidx.room.*

@Entity(
    tableName = "playground_sport",
    foreignKeys = [ForeignKey(
        entity = Sport::class,
        parentColumns = ["id"],
        childColumns = ["sport_id"]
    ),
        ForeignKey(
            entity = SportCenter::class,
            parentColumns = ["id"],
            childColumns = ["sport_center_id"]
        )],
    indices = [Index(value = [ "sport_id", "sport_center_id"], unique = true)]
)
data class PlaygroundSport(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "sport_id", index = true)
    val sportId: Int,
    @ColumnInfo(name = "sport_center_id", index = true)
    val sportCenterId: Int,
    @ColumnInfo(name = "playground_name")
    val playgroundName: String,
    @ColumnInfo(name = "cost_per_hour")
    val pricePerHour: Float,
)




