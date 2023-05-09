package it.polito.mad.sportapp.localDB.dao

import androidx.room.*
import it.polito.mad.sportapp.entities.SportLevel
import it.polito.mad.sportapp.entities.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query(
        "SELECT * FROM user WHERE first_name LIKE :first AND " +
                "last_name LIKE :last LIMIT 1"
    )
    fun findByName(first: String, last: String): User

    @Query("SELECT username FROM user WHERE id == :id")
    fun findUsernameById(id: Int): String
    @Query("SELECT * FROM user WHERE username == :username LIMIT 1")
    fun findByUsername(username: String): User

    @Query("SELECT * FROM user WHERE id == :id LIMIT 1")
    fun findById(id: Int): User

    @Query("SELECT S.name, US.level, US.sport_id FROM user_sport AS US INNER JOIN sport AS S ON US.sport_id = S.id WHERE US.user_id LIKE :userId")
    fun findSportByUserId(userId: Int): List<SportLevel>

    @Query("SELECT COUNT(id) AS count  FROM playground_reservation WHERE user_id == :userId AND datetime(end_date_time) < datetime('now') GROUP BY sport_id")
    fun findPlayedSports(userId: Int): List<Int>

    @Query("SELECT COUNT(id) FROM playground_reservation WHERE user_id == :userId AND datetime(end_date_time) < datetime('now')")
    fun findPlayedMatches(userId: Int): Int

    @Query("DELETE FROM user_sport WHERE user_id == :userId")
    fun deleteSportByUserId(userId: Int)

    @Query("INSERT INTO user_sport (user_id, sport_id, level) VALUES (:userId, :sportId, :level)")
    fun insertSportByUserId(userId: Int, sportId: Int, level: String)

    @Insert
    fun insertAll(vararg users: User)

    @Insert
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)

}


