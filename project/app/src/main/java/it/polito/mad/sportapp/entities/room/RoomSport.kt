package it.polito.mad.sportapp.entities.room

import androidx.room.*

@Entity(tableName = "sport")
data class RoomSport(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "emoji")
    val emoji: String,
    @ColumnInfo(name = "max_players")
    val maxPlayers: Int
) {
    override fun toString(): String {
        return printWithEmoji(onTheLeft = false)
    }

    fun printWithEmoji(onTheLeft: Boolean = false): String {
        return if(onTheLeft) "$emoji  $name" else "$name  $emoji"
    }
}