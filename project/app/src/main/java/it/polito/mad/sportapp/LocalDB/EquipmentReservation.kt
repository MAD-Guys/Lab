package it.polito.mad.sportapp.LocalDB

import androidx.room.*

@Entity(tableName = "equipment_reservation")
data class EquipmentReservation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "reservation_id")
    val reservationId: Int,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: Int,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
)

@Entity(foreignKeys = [ForeignKey(entity = PlaygroundReservationDao::class, parentColumns = ["id"], childColumns = ["reservation_id"]),
    ForeignKey(entity = Equipment::class, parentColumns = ["id"], childColumns = ["equipment_id"])])

@Dao
interface EquipmentReservationDao {
    @Query("SELECT * FROM equipment_reservation")
    fun getAll(): List<EquipmentReservation>

    @Query("SELECT * FROM equipment_reservation WHERE reservation_id LIKE :reservationId")
    fun findByReservationId(reservationId: Int): List<EquipmentReservation>

    @Query("SELECT * FROM equipment_reservation WHERE equipment_id LIKE :equipmentId")
    fun findByEquipmentId(equipmentId: Int): List<EquipmentReservation>

    @Insert
    fun insertAll(vararg equipmentReservation: EquipmentReservation)

    @Delete
    fun delete(equipmentReservation: EquipmentReservation)
}
