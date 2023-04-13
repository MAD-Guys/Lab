package it.polito.mad.sportapp.LocalDB

import androidx.room.*

@Entity(tableName = "sports")
data class Sport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "max_players")
    val maxPlayers: Int,
)

@Dao
interface SportDao {
    @Query("SELECT * FROM sports")
    fun getAll(): List<Sport>

    @Query("SELECT * FROM sports WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): Sport

    @Insert
    fun insertAll(vararg sports: Sport)

    @Delete
    fun delete(sport: Sport)
}