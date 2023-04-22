package it.polito.mad.sportapp.localDB

import androidx.room.*

@Entity(tableName = "sport_center")
data class SportCenter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "location")
    val location: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    @ColumnInfo(name = "opening_hours")
    val openingHours: String,
    @ColumnInfo(name = "closing_hours")
    val closingHours: String,
)

@Dao
interface SportCenterDao {
    @Query("SELECT * FROM sport_center")
    fun getAll(): List<SportCenter>

    @Query("SELECT * FROM sport_center WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): SportCenter

    @Insert
    fun insertAll(vararg sportCenter: SportCenter)

    @Delete
    fun delete(sportCenter: SportCenter)
}