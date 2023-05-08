package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.polito.mad.sportapp.entities.DetailedReservationForAvailablePlaygrounds
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.PlaygroundReservation

@Dao
interface ReservationDao {

    @Query("SELECT * FROM playground_reservation")
    fun getAll(): List<PlaygroundReservation>

    @Query("SELECT * FROM playground_reservation WHERE playground_id == :playgroundId")
    fun findByPlaygroundId(playgroundId: Int): List<PlaygroundReservation>

    @Query("SELECT PR.id, PR.user_id, PR.playground_id , U.username, SC.name AS sport_center_name, SC.address, S.name AS sport_name," +
            "PR.start_date_time, PR.end_date_time, PS.playground_name, PR.total_price, PS.sport_id, PS.sport_center_id " +
            " FROM sport AS S, playground_sport AS PS, playground_reservation as PR, sport_center AS SC, USER AS U " +
            "WHERE PR.sport_id = S.id AND PR.playground_id = PS.id AND PR.sport_center_id = SC.id AND PR.user_id = :userId AND user_id = U.id")
    fun findByUserId(userId: Int): List<DetailedReservation>

    @Query("SELECT PR.id, PR.user_id, PR.playground_id, U.username, SC.name AS sport_center_name, SC.address, S.name AS sport_name," +
            "PR.start_date_time, PR.end_date_time, PS.playground_name, PR.total_price, PS.sport_id, PS.sport_center_id " +
            " FROM sport AS S, playground_sport AS PS, playground_reservation as PR, sport_center AS SC, user AS U " +
            "WHERE PR.sport_id = S.id AND PR.playground_id = PS.id AND PR.sport_center_id = SC.id AND PR.sport_id = :sportId AND user_id = U.id")
    fun findBySportId(sportId: Int): List<DetailedReservation>

    @Query("SELECT * FROM playground_reservation WHERE id == :id LIMIT 1")
    fun findById(id: Int): PlaygroundReservation

    @Query(
        "SELECT PR.id, PR.user_id, PR.playground_id, U.username, SC.name AS sport_center_name, SC.address, S.name AS sport_name , " +
                "PR.start_date_time, PR.end_date_time, PS.playground_name, PR.total_price, PS.sport_id, PS.sport_center_id " +
                "FROM sport AS S, playground_sport AS PS, playground_reservation as PR, sport_center AS SC, user AS U " +
                "WHERE PR.sport_id = S.id AND PR.playground_id = PS.id AND PR.sport_center_id = SC.id AND PR.id = :id AND user_id = U.id"
    )
    fun findDetailedReservationById(id: Int): DetailedReservation
    @Query("SELECT PR.start_date_time , PR.end_date_time, PS.id AS playground_id, PS.sport_id, SC.name AS sport_center_name, PS.playground_name, PS.cost_per_hour AS price_per_hour " +
            "FROM playground_reservation AS PR, PLAYGROUND_SPORT AS PS, sport_center AS SC " +
            "WHERE PR.playground_id = PS.Id AND " +
            "SC.Id = PS.sport_center_id AND " +
            "PS.sport_id == :sportId AND " +
            "PR.start_date_time LIKE :yearMonth || '%'")
    fun findPlaygroundsBySportIdAndDate(sportId: Int, yearMonth: String): List<DetailedReservationForAvailablePlaygrounds>

    @Insert
    fun insertAll(vararg playgroundReservations: PlaygroundReservation)

    @Insert
    fun insert(playgroundReservation: PlaygroundReservation)

    @Query("UPDATE playground_reservation SET total_price = total_price + :price WHERE id LIKE :reservationId")
    fun increasePrice(reservationId: Int, price: Float)

    @Query("UPDATE playground_reservation SET total_price = total_price - :price WHERE id LIKE :reservationId")
    fun reducePrice(reservationId: Int, price: Float)

    @Query("DELETE FROM playground_reservation WHERE id LIKE :reservationId")
    fun deleteById(reservationId: Int)


}