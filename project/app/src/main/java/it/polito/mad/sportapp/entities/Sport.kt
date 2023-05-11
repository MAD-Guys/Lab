package it.polito.mad.sportapp.entities

import androidx.room.*

@Entity(tableName = "sport")
data class Sport(
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
        return name
    }
}

