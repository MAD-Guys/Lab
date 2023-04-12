package it.polito.mad.lab2

import android.graphics.Bitmap
import androidx.room.*

@Entity(tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "first_name")
    val firstName: String,
    @ColumnInfo(name = "last_name")
    val lastName: String,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "gender")
    val gender: String,
    @ColumnInfo(name= "age")
    val age: Int,
    @ColumnInfo(name = "location")
    val location: String,
    @ColumnInfo(name = "bio")
    val bio: String,
    @ColumnInfo(name = "profile_picture")
    val profilePicture: String,
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM users WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}


