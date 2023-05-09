package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.polito.mad.sportapp.entities.Sport

@Dao
interface SportDao {
    @Query("SELECT * FROM sport")
    fun getAll(): List<Sport>

    @Query("SELECT COUNT(*) FROM sport")
    fun count(): Int

    @Insert
    fun insertAll(vararg sport: Sport)

    @Insert
    fun insert(sport: Sport)


}