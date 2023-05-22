package it.polito.mad.sportapp.entities

import androidx.room.*


@Entity(tableName = "user", indices = [Index(value = ["username"], unique = true)] )
data class User (
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
    var sportLevel: List<SportLevel> = listOf()

    @Ignore
    var achievements : Map<Achievement,Boolean> = mapOf(
        Achievement.AtLeastOneSport to false,
        Achievement.AtLeastFiveSports to false,
        Achievement.AllSports to false,
        Achievement.AtLeastThreeMatches to false,
        Achievement.AtLeastTenMatches to false,
        Achievement.AtLeastTwentyFiveMatches to false,
    )

}

