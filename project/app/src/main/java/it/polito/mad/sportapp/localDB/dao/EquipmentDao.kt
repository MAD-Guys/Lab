package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.polito.mad.sportapp.entities.Equipment

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment")
    fun getAll(): List<Equipment>


    @Query("SELECT * FROM equipment WHERE sport_center_id LIKE :sportCenterId AND availability > 0")
    fun findBySportCenterId(sportCenterId: Int): List<Equipment>

    @Query("SELECT * FROM equipment WHERE sport_center_id LIKE :sportCenterId AND sport_id LIKE :sportId AND availability > 0")
    fun findBySportCenterIdAndSportId(sportCenterId: Int, sportId: Int): List<Equipment>

    @Insert
    fun insert(equipment: Equipment)

    @Insert
    fun insertAll(vararg equipment: Equipment)

    @Query("UPDATE equipment SET availability = availability - 1 WHERE id LIKE :equipmentId")
    fun reduceAvailability(equipmentId: Int)

    @Query("UPDATE equipment SET availability = availability + 1 WHERE id LIKE :equipmentId")
    fun increaseAvailability(equipmentId: Int)

    @Query("UPDATE equipment_reservation SET quantity = quantity + 1, total_price = total_price + :price WHERE equipment_id LIKE :equipmentId AND playground_reservation_id LIKE :playgroundReservationId")
    fun increaseQuantity(equipmentId: Int, playgroundReservationId: Int, price: Float)

    @Query("UPDATE equipment_reservation SET quantity = quantity - 1, total_price = total_price - :price WHERE equipment_id LIKE :equipmentId AND playground_reservation_id LIKE :playgroundReservationId")
    fun reduceQuantity(equipmentId: Int, playgroundReservationId: Int, price: Float)

    @Query("SELECT unit_price FROM equipment WHERE id LIKE :equipmentId")
    fun findPriceById(equipmentId: Int): Float

    @Query("DELETE FROM equipment_reservation WHERE equipment_id LIKE :equipmentId AND playground_reservation_id LIKE :playgroundReservationId")
    fun deleteReservation(equipmentId: Int, playgroundReservationId: Int)
}