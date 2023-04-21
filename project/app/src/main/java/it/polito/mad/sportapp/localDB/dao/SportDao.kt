package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Query
import it.polito.mad.sportapp.entities.Sport

@Dao
interface SportDao {
    @Query("SELECT * FROM sport")
    fun getAll(): List<Sport>


}