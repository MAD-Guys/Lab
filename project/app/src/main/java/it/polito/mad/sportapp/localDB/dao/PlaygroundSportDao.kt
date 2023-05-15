package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.polito.mad.sportapp.entities.DetailedPlayground
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.PlaygroundSport


@Dao
interface PlaygroundSportDao {

    @Insert
    fun insert(playgroundSport: PlaygroundSport)

    @Insert
    fun insertAll(vararg playgroundSports: PlaygroundSport)

    @Query("SELECT * FROM playground_sport")
    fun getAll(): List<PlaygroundSport>

    @Query("SELECT PS.sport_id, S.name AS sport_name, SC.id AS sport_center_id, SC.name AS sport_center_name, PS.playground_name, PS.cost_per_hour, SC.opening_hour, SC.closing_hour , PS.id AS playground_id " +
            "FROM sport_center SC, playground_sport PS, sport S " +
            "WHERE SC.id == PS.sport_center_id AND PS.sport_id == :sportId AND S.id == PS.sport_id")
    fun findBySportId(sportId: Int): List<DetailedPlayground>

    @Query("SELECT PS.id AS playground_id, PS.playground_name, SC.id AS sport_center_id, SC.name AS sport_center_name, PS.sport_id, S.name AS sport_name, S.emoji AS sport_emoji,  SC.address AS sport_center_address, SC.opening_hour AS opening_time, SC.closing_hour AS closing_time, PS.cost_per_hour AS price_per_hour " +
            "FROM sport_center SC, playground_sport PS, SPORT S " +
            "WHERE SC.id == PS.sport_center_id AND S.id = PS.sport_id AND PS.id == :playgroundId")
    fun getPlaygroundInfo(playgroundId: Int): PlaygroundInfo

    @Query ("SELECT cost_per_hour FROM playground_sport WHERE id == :playgroundId")
    fun getPlaygroundSportPricePerHour(playgroundId: Int): Float

}