package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.EquipmentReservation

@Dao
interface EquipmentDao {


    @Query("SELECT * FROM equipment WHERE sport_center_id LIKE :sportCenterId AND availability > 0")
    fun findBySportCenterId(sportCenterId: Int): List<Equipment>

    @Query("SELECT * FROM equipment WHERE sport_center_id LIKE :sportCenterId AND sport_id LIKE :sportId AND availability > 0")
    fun findBySportCenterIdAndSportId(sportCenterId: Int, sportId: Int): List<Equipment>

    @Query("SELECT * FROM equipment_reservation  WHERE id == :equipmentReservationId ")
    fun findEquipmentReservationById(equipmentReservationId: Int): EquipmentReservation

    @Query("SELECT * FROM equipment_reservation  WHERE playground_reservation_id == :playgroundId")
    fun findEquipmentReservationByPlaygroundId(playgroundId: Int): List<EquipmentReservation>
    @Insert
    fun insertEquipmentReservation(equipmentReservation: EquipmentReservation)

    @Insert
    fun insertEquipment(equipment : Equipment)

    @Query ("SELECT name FROM equipment WHERE id == :equipmentId")
    fun findEquipmentNameById(equipmentId: Int): String

    @Query("UPDATE equipment SET availability = availability - :n WHERE id LIKE :equipmentId")
    fun reduceAvailabilityOfN(equipmentId: Int, n: Int)



    @Query("UPDATE equipment SET availability = availability + :n WHERE id LIKE :equipmentId")
    fun increaseAvailabilityOfN(equipmentId: Int, n: Int)

    @Query("UPDATE equipment_reservation SET quantity = quantity + :n, total_price = total_price + :price WHERE equipment_id LIKE :equipmentId AND playground_reservation_id LIKE :playgroundReservationId")
    fun increaseQuantityOfN(equipmentId: Int, playgroundReservationId: Int, price: Float, n: Int)

    @Query("UPDATE equipment_reservation SET quantity = quantity - :n, total_price = total_price - :price WHERE equipment_id LIKE :equipmentId AND playground_reservation_id LIKE :playgroundReservationId")
    fun reduceQuantityOfN(equipmentId: Int, playgroundReservationId: Int, price: Float, n: Int)

    @Query("SELECT unit_price FROM equipment WHERE id LIKE :equipmentId")
    fun findPriceById(equipmentId: Int): Float

    @Query("DELETE FROM equipment_reservation WHERE equipment_id LIKE :equipmentId AND playground_reservation_id LIKE :playgroundReservationId")
    fun deleteReservation(equipmentId: Int, playgroundReservationId: Int)
}