package it.polito.mad.sportapp.entities

import androidx.room.*

@Entity(tableName = "sport_center",)
data class SportCenter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    @ColumnInfo(name = "opening_hour")
    val openingHours: String,
    @ColumnInfo(name = "closing_hour")
    val closingHours: String,
)

