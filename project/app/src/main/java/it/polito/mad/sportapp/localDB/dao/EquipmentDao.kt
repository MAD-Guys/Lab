package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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
    fun insertAll(vararg equipment: Equipment)

    @Delete
    fun delete(equipment: Equipment)
}