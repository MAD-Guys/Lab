package it.polito.mad.sportapp.LocalDB

import androidx.room.*

@Entity(tableName = "playground_sports")
data class PlaygroundSport (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "playground_id")
    val playgroundId: Int,
    @ColumnInfo(name = "sport_id")
    val sportId: Int,
    @ColumnInfo(name = "price")
    val price: Float,
        )

@Entity(foreignKeys = [ForeignKey(entity = Playground::class, parentColumns = ["id"], childColumns = ["playground_id"]), ForeignKey(entity = Sport::class, parentColumns = ["id"], childColumns = ["sport_id"])])

@Dao
interface PlaygroundSportDao {
    @Query("SELECT * FROM playground_sports")
    fun getAll(): List<PlaygroundSport>

    @Query("SELECT * FROM playground_sports WHERE playground_id LIKE :playgroundId")
    fun findByPlaygroundId(playgroundId: Int): List<PlaygroundSport>

    @Query("SELECT * FROM playground_sports WHERE sport_id LIKE :sportId")
    fun findBySportId(sportId: Int): List<PlaygroundSport>

    @Insert
    fun insertAll(vararg playgroundSports: PlaygroundSport)

    @Delete
    fun delete(playgroundSport: PlaygroundSport)
}
