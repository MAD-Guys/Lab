package it.polito.mad.sportapp.localDB

import androidx.room.*

@Entity(tableName = "playgrounds")
data class Playground(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "sport_center_id")
    val sportCenterId: Int,
    @ColumnInfo(name = "name")
    val name: String,
)

@Entity(foreignKeys = [ForeignKey(entity = SportCenter::class, parentColumns = ["id"], childColumns = ["sport_center_id"])])

@Dao
interface PlaygroundDao {
    @Query("SELECT * FROM playgrounds")
    fun getAll(): List<Playground>

    @Query("SELECT * FROM playgrounds WHERE sport_center_id LIKE :sportCenterId")
    fun findBySportCenterId(sportCenterId: Int): List<Playground>

    @Insert
    fun insertAll(vararg playgrounds: Playground)

    @Delete
    fun delete(playground: Playground)
}
