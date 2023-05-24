package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.polito.mad.sportapp.entities.room.RoomSportCenter

@Dao
interface SportCenterDao {
    @Query("SELECT * FROM sport_center")
    fun getAll(): List<RoomSportCenter>

    @Query("SELECT * FROM sport_center WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): RoomSportCenter

    @Query("SELECT * FROM sport_center WHERE id == :id LIMIT 1")
    fun findById(id: Int): RoomSportCenter




    //Useful queries to populate the database
    @Insert
    fun insertAllSportCenter(vararg sportCenters: RoomSportCenter)




}