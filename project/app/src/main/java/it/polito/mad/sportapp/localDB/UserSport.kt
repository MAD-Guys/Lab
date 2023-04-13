package it.polito.mad.sportapp.localDB

import androidx.room.*
import it.polito.mad.sportapp.User

@Entity(tableName = "user_sport")
data class UserSport (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "sport_id")
    val sportId: Int,
    @ColumnInfo(name = "level")
    val level: Int,
)
@Entity(foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["user_id"]),
    ForeignKey(entity = Sport::class, parentColumns = ["id"], childColumns = ["sport_id"])])

@Dao
interface UserSportDao {
    @Query("SELECT * FROM user_sport")
    fun getAll(): List<UserSport>

    @Query("SELECT * FROM user_sport WHERE user_id LIKE :userId")
    fun findByUserId(userId: Int): List<UserSport>

    @Query("SELECT * FROM user_sport WHERE sport_id LIKE :sportId")
    fun findBySportId(sportId: Int): List<UserSport>

    @Insert
    fun insertAll(vararg userSport: UserSport)

    @Delete
    fun delete(userSport: UserSport)
}