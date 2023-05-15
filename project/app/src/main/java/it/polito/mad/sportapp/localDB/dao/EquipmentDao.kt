package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.polito.mad.sportapp.entities.DetailedEquipmentReservation
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.EquipmentReservation

@Dao
interface EquipmentDao {

    @Query("SELECT Count(ER.id) FROM equipment_reservation AS ER, playground_reservation AS PR WHERE ER.playground_reservation_id = PR.id AND PR.playground_id =:playgroundId AND ER.equipment_id =:equipmentId ")
    fun getEquipmentIfAvailable(playgroundId:Int, equipmentId:Int):Int
    @Query("SELECT * FROM equipment WHERE sport_center_id LIKE :sportCenterId AND availability > 0")
    fun findBySportCenterId(sportCenterId: Int): List<Equipment>

    @Query("SELECT * FROM equipment WHERE sport_center_id LIKE :sportCenterId AND sport_id LIKE :sportId AND availability > 0")
    fun findBySportCenterIdAndSportId(sportCenterId: Int, sportId: Int): List<Equipment>

    @Query(
        "SELECT ER.playground_reservation_id AS playground_reservation_id, E.id AS equipment_id, E.name AS equipment_name, ER.quantity AS selected_quantity, E.unit_price AS unit_price " +
        "FROM equipment_reservation ER, equipment E " +
                "WHERE E.id == ER.equipment_id AND ER.playground_reservation_id == :reservationId "
    )
    fun findReservationEquipmentsByReservationId(reservationId: Int): List<DetailedEquipmentReservation>

    @Query("SELECT * FROM equipment_reservation  WHERE playground_reservation_id == :playgroundId")
    fun findEquipmentReservationByPlaygroundId(playgroundId: Int): List<EquipmentReservation>
    @Insert
    fun insertEquipmentReservation(equipmentReservation: EquipmentReservation)

    @Insert
    fun insertEquipment(equipment : Equipment)

    @Query("DELETE FROM equipment_reservation WHERE playground_reservation_id == :playgroundReservationId")
    fun deleteEquipmentReservationByPlaygroundReservationId(playgroundReservationId: Int)

    @Query ("SELECT name FROM equipment WHERE id == :equipmentId")
    fun findEquipmentNameById(equipmentId: Int): String

    @Query("SELECT unit_price FROM equipment WHERE id == :equipmentId")
    fun getEquipmentUnitPrice(equipmentId: Int): Float

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