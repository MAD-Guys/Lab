package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.polito.mad.sportapp.entities.room.RoomSport

@Dao
interface SportDao {
    @Query("SELECT * FROM sport")
    fun getAll(): List<RoomSport>

    @Query("SELECT COUNT(*) FROM sport")
    fun count(): Int

    @Insert
    fun insertAll(vararg sport: RoomSport)

    @Insert
    fun insert(sport: RoomSport)


}