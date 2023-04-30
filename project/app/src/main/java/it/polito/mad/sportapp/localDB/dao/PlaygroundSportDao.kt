package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import it.polito.mad.sportapp.entities.PlaygroundSport


@Dao
interface PlaygroundSportDao {

    @Insert
    fun insert(playgroundSport: PlaygroundSport)

    @Insert
    fun insertAll(vararg playgroundSports: PlaygroundSport)

}