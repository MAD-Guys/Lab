package it.polito.mad.sportapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "review",

    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"]
    ),
        ForeignKey(
            entity = PlaygroundSport::class,
            parentColumns = ["id"],
            childColumns = ["playground_id"]
        )]
)
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "user_id", index = true)
    val userId: Int,
    @ColumnInfo(name = "playground_id", index = true)
    val playgroundId: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "quality_rating")
    val qualityRating: Float,
    @ColumnInfo(name = "facilities_rating")
    val facilitiesRating: Float,
    @ColumnInfo(name = "review")
    val review: String,
    @ColumnInfo(name = "timestamp")
    var timestamp: String,
    @ColumnInfo(name = "last_update")
    var lastUpdate: String,
){
    @Ignore
    var publicationDate: LocalDateTime = LocalDateTime.parse(timestamp)
    @Ignore
    var lastUpdateDate: LocalDateTime = LocalDateTime.parse(lastUpdate)
    @Ignore
    var username : String = ""
}
