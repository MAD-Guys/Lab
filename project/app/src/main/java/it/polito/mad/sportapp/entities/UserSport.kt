package it.polito.mad.sportapp.entities

import androidx.room.*

@Entity(
    tableName = "user_sport",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"]
    ),
        ForeignKey(
            entity = Sport::class,
            parentColumns = ["id"],
            childColumns = ["sport_id"])],
    indices = [Index(value = ["user_id", "sport_id"], unique = true)]
)
data class UserSport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "sport_id", index = true)
    val sportId: Int,
    @ColumnInfo(name = "level")
    val level: String,
)


