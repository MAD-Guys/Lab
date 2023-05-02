package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.polito.mad.sportapp.entities.DetailedPlayground
import it.polito.mad.sportapp.entities.PlaygroundSport


@Dao
interface PlaygroundSportDao {

    @Insert
    fun insert(playgroundSport: PlaygroundSport)

    @Insert
    fun insertAll(vararg playgroundSports: PlaygroundSport)

    @Query("SELECT * FROM playground_sport")
    fun getAll(): List<PlaygroundSport>

    @Query("SELECT PS.sport_id, SC.name AS sport_center_name, PS.playground_name, PS.cost_per_hour, SC.opening_hour, SC.closing_hour , PS.id AS playground_id " +
            "FROM sport_center SC, playground_sport PS " +
            "WHERE SC.id == PS.sport_center_id AND PS.sport_id == :sportId")
    fun findBySportId(sportId: Int): List<DetailedPlayground>

}