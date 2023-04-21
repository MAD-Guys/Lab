package it.polito.mad.sportapp.entities

import androidx.room.*

@Entity(tableName = "sport")
data class Sport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "max_players")
    val maxPlayers: Int,
)

