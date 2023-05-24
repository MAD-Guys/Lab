package it.polito.mad.sportapp.entities.room

import androidx.room.*

@Entity(
    tableName = "user_sport",
    foreignKeys = [ForeignKey(
        entity = RoomUser::class,
        parentColumns = ["id"],
        childColumns = ["user_id"]
    ),
        ForeignKey(
            entity = RoomSport::class,
            parentColumns = ["id"],
            childColumns = ["sport_id"])],
)
data class RoomUserSport(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "user_id", index = true)
    val userId: Int,
    @ColumnInfo(name = "sport_id", index = true)
    val sportId: Int,
    @ColumnInfo(name = "level")
    val level: String,
)


