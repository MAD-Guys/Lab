package it.polito.mad.lab2.LocalDB

import androidx.room.*

@Entity(tableName = "equipment")
data class Equipment (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "sport_id")
    val sportId: Int,
    @ColumnInfo(name = "sportcenter_id")
    val sportcenterId: Int,
    @ColumnInfo(name = "price")
    val price: Float,
    @ColumnInfo(name = "availability")
    val availability: Int,
)

@Entity(foreignKeys = [ForeignKey(entity = Sport::class, parentColumns = ["id"], childColumns = ["sport_id"]), ForeignKey(entity = SportCenter::class, parentColumns = ["id"], childColumns = ["sportcenter_id"])])

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment")
    fun getAll(): List<Equipment>

    @Query("SELECT * FROM equipment WHERE sport_id LIKE :sportId")
    fun findBySportId(sportId: Int): List<Equipment>

    @Query("SELECT * FROM equipment WHERE sportcenter_id LIKE :sportcenterId")
    fun findBySportcenterId(sportcenterId: Int): List<Equipment>

    @Insert
    fun insertAll(vararg equipments: Equipment)

    @Delete
    fun delete(equipment: Equipment)
}