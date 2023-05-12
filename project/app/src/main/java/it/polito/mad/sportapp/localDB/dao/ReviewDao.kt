package it.polito.mad.sportapp.localDB.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.polito.mad.sportapp.entities.Review

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review WHERE playground_id == :playgroundId")
    fun findByPlaygroundId(playgroundId: Int): List<Review>

    @Query("SELECT * FROM review WHERE user_id == :userId AND playground_id == :playgroundId")
    fun findByUserIdAndPlaygroundId(userId: Int, playgroundId: Int): Review


    @Update
    fun update(review: Review)

    @Insert
    fun insert(review: Review)

    @Delete
    fun delete(review: Review)

}