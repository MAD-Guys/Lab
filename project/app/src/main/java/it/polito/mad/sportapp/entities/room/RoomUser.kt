package it.polito.mad.sportapp.entities.room

import androidx.room.*


@Entity(tableName = "user", indices = [Index(value = ["username"], unique = true)] )
data class RoomUser (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
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
) {
    @Ignore
    var sportLevel: List<RoomSportLevel> = listOf()

    @Ignore
    var achievements : Map<RoomAchievement,Boolean> = mapOf(
        RoomAchievement.AtLeastOneSport to false,
        RoomAchievement.AtLeastFiveSports to false,
        RoomAchievement.AllSports to false,
        RoomAchievement.AtLeastThreeMatches to false,
        RoomAchievement.AtLeastTenMatches to false,
        RoomAchievement.AtLeastTwentyFiveMatches to false,
    )

}

