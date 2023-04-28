package it.polito.mad.sportapp.entities

import androidx.room.*
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(
    tableName = "playground_reservation",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"]
    ),
        ForeignKey(
            entity = Sport::class,
            parentColumns = ["id"],
            childColumns = ["sport_id"]
        ),
        ForeignKey(
            entity = PlaygroundSport::class,
            parentColumns = ["id"],
            childColumns = ["playground_id"]
        ),
        ForeignKey(
            entity = SportCenter::class,
            parentColumns = ["id"],
            childColumns = ["sport_center_id"]
        )],
)
data class PlaygroundReservation(
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
) {
    @Ignore
    val duration = LocalTime.parse(endDateTime.substring(11, 19)).minute - LocalTime.parse(
        startDateTime.substring(11, 19)
    ).minute
}





