package it.polito.mad.sportapp.entities.room

import androidx.room.*

@Entity(
    tableName = "playground_reservation",
    foreignKeys = [ForeignKey(
        entity = RoomUser::class,
        parentColumns = ["id"],
        childColumns = ["user_id"]
    ),
        ForeignKey(
            entity = RoomSport::class,
            parentColumns = ["id"],
            childColumns = ["sport_id"]
        ),
        ForeignKey(
            entity = RoomPlaygroundSport::class,
            parentColumns = ["id"],
            childColumns = ["playground_id"]
        ),
        ForeignKey(
            entity = RoomSportCenter::class,
            parentColumns = ["id"],
            childColumns = ["sport_center_id"]
        )],
)
data class RoomPlaygroundReservation(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "playground_id", index = true)
    val playgroundId: Int,
    @ColumnInfo(name = "user_id", index = true)
    val userId: Int,
    @ColumnInfo(name = "sport_id", index = true)
    val sportId: Int,
    @ColumnInfo(name = "sport_center_id", index = true)
    val sportCenterId: Int,
    @ColumnInfo(name = "start_date_time")
    val startDateTime: String,
    @ColumnInfo(name = "end_date_time")
    val endDateTime: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: String,
    @ColumnInfo(name = "total_price")
    val totalPrice: Float,
)





