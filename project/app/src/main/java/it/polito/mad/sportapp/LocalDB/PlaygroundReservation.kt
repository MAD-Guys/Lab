package it.polito.mad.sportapp.LocalDB

import androidx.room.*
import it.polito.mad.sportapp.User

@Entity(tableName = "playground_reservation")
data class PlaygroundReservation (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "playground_id")
    val playgroundId: Int,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "sport_id")
    val sportId: Int,
    @ColumnInfo(name = "date_time")
    val date: String,
    @ColumnInfo(name = "duration")
    val duration: Int,
    @ColumnInfo(name = "timestamp")
    val timestamp: String,
        )

@Entity(foreignKeys = [ForeignKey(entity = Playground::class, parentColumns = ["id"], childColumns = ["playground_id"]),
    ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["user_id"]),
    ForeignKey(entity = Sport::class, parentColumns = ["id"], childColumns = ["sport_id"])])

@Dao
interface PlaygroundReservationDao {
    @Query("SELECT * FROM playground_reservation")
    fun getAll(): List<PlaygroundReservation>

    @Query("SELECT * FROM playground_reservation WHERE playground_id LIKE :playgroundId")
    fun findByPlaygroundId(playgroundId: Int): List<PlaygroundReservation>

    @Query("SELECT * FROM playground_reservation WHERE user_id LIKE :userId")
    fun findByUserId(userId: Int): List<PlaygroundReservation>

    @Query("SELECT * FROM playground_reservation WHERE sport_id LIKE :sportId")
    fun findBySportId(sportId: Int): List<PlaygroundReservation>

    @Insert
    fun insertAll(vararg playgroundReservation: PlaygroundReservation)

    @Delete
    fun delete(playgroundReservation: PlaygroundReservation)
}
