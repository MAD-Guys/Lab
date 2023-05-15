package it.polito.mad.sportapp.entities

import androidx.room.ColumnInfo
import androidx.room.Ignore
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

//Useful classes for the queries in join operations

//data class for the join between user and sport to get the level of the sport played by the user
data class SportLevel(
    @ColumnInfo(name = "sport_id")
    val sportId: Int,
    @ColumnInfo(name = "name")
    val sport: String?,
    @ColumnInfo(name = "level")
    val level: String?

)

data class DetailedReservation(
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "sport_center_id")
    val sportCenterId: Int,
    @ColumnInfo(name = "sport_id")
    val sportId: Int,
    @ColumnInfo(name = "sport_emoji")
    val sportEmoji: String,
    @ColumnInfo(name = "sport_center_name")
    val sportCenterName: String,
    @ColumnInfo(name = "address")
    val location: String,
    @ColumnInfo(name = "sport_name")
    val sportName: String,
    @ColumnInfo(name = "start_date_time")
    val startDateTime: String,
    @ColumnInfo(name = "end_date_time")
    val endDateTime: String,
    @ColumnInfo(name = "playground_id")
    val playgroundId: Int,
    @ColumnInfo(name = "playground_name")
    val playgroundName: String,
    @ColumnInfo(name = "playground_price_per_hour")
    val playgroundPricePerHour: Float,
    @ColumnInfo(name = "total_price")
    val totalPrice: Float,
) {
    @Ignore
    var date: LocalDate = LocalDate.parse(startDateTime.substring(0, 10))

    @Ignore
    var startTime: LocalTime = LocalTime.parse(startDateTime.substring(11, 19))

    @Ignore
    var startLocalDateTime: LocalDateTime = LocalDateTime.parse(startDateTime)

    @Ignore
    var endLocalDateTime: LocalDateTime = LocalDateTime.parse(endDateTime)

    @Ignore
    var endTime: LocalTime = LocalTime.parse(endDateTime.substring(11, 19))

    @Ignore
    var equipments: MutableList<DetailedEquipmentReservation> = mutableListOf()

    @Ignore
    val duration = Duration.between(
        LocalTime.parse(startDateTime.substring(11, 19)), LocalTime.parse(
            endDateTime.substring(11, 19)
        )
    ).toMinutes()

    @Ignore
    var endSlot: LocalDateTime = endLocalDateTime.minusMinutes(duration)
    @Ignore
    var startSlot: LocalDateTime = startLocalDateTime


    fun printSportNameWithEmoji(emojiOnTheLeft: Boolean = false): String {
        return if(emojiOnTheLeft) "$sportEmoji $sportName" else "$sportName $sportEmoji"
    }
}

data class DetailedReservationForAvailablePlaygrounds(
    @ColumnInfo(name = "start_date_time")
    val startDateTime: String,
    @ColumnInfo(name = "end_date_time")
    val endDateTime: String,
    @ColumnInfo(name = "playground_id")
    val playgroundId: Int,
    @ColumnInfo(name = "sport_id")
    val sportId: Int,
    @ColumnInfo(name = "sport_name")
    val sportName: String,
    @ColumnInfo(name = "playground_name")
    val playgroundName: String,
    @ColumnInfo(name = "sport_center_id")
    val sportCenterId: Int,
    @ColumnInfo(name = "sport_center_name")
    val sportCenterName: String,
    @ColumnInfo(name = "price_per_hour")
    val pricePerHour: Float,
) {
    @Ignore
    var date: LocalDate = LocalDate.parse(startDateTime.substring(0, 10))

    @Ignore
    var startLocalDateTime: LocalDateTime = LocalDateTime.parse(startDateTime)

    @Ignore
    var endLocalDateTime: LocalDateTime = LocalDateTime.parse(endDateTime)
}

data class DetailedPlayground(
    @ColumnInfo(name = "sport_id", index = true)
    val sportId: Int,
    @ColumnInfo(name = "sport_name")
    val sportName: String,
    @ColumnInfo(name = "sport_center_id")
    val sportCenterId: Int,
    @ColumnInfo(name = "sport_center_name", index = true)
    val sportCenterName: String,
    @ColumnInfo(name = "playground_name")
    val playgroundName: String,
    @ColumnInfo(name = "cost_per_hour")
    val pricePerHour: Float,
    @ColumnInfo(name = "opening_hour")
    val openingHours: String,
    @ColumnInfo(name = "closing_hour")
    val closingHours: String,
    @ColumnInfo(name = "playground_id")
    val playgroundId: Int,
) {
    @Ignore
    val openingTime: LocalTime = LocalTime.parse(openingHours)

    @Ignore
    val closingTime: LocalTime = LocalTime.parse(closingHours)
}

data class PlaygroundInfo(
    @ColumnInfo(name = "playground_id")
    val playgroundId: Int,
    @ColumnInfo(name = "playground_name")
    val playgroundName: String,
    @ColumnInfo(name ="sport_center_id")
    val sportCenterId: Int,
    @ColumnInfo(name = "sport_center_name")
    val sportCenterName: String,
    @ColumnInfo(name = "sport_id")
    val sportId: Int,
    @ColumnInfo(name = "sport_name")
    val sportName: String,
    @ColumnInfo(name = "sport_emoji")
    val sportEmoji: String,
    @ColumnInfo(name = "sport_center_address")
    val sportCenterAddress: String,
    @ColumnInfo(name = "opening_time")
    val openingTime: String,
    @ColumnInfo(name = "closing_time")
    val closingTime: String,
    @ColumnInfo(name = "price_per_hour")
    val pricePerHour: Float


) {
    @Ignore
    var overallQualityRating: Float = 0f

    @Ignore
    var overallFacilitiesRating: Float = 0f

    @Ignore
    val openingHours: LocalTime = LocalTime.parse(openingTime)

    @Ignore
    val closingHours: LocalTime = LocalTime.parse(closingTime)

    @Ignore
    var overallRating: Float = (overallQualityRating + overallFacilitiesRating) / 2

    @Ignore
    var reviewList: List<Review> = listOf()
}

enum class Achievement{
    AtLeastOneSport,
    AtLeastFiveSports,
    AllSports,
    AtLeastThreeMatches,
    AtLeastTenMatches,
    AtLeastTwentyFiveMatches
}

data class DetailedEquipmentReservation (
    @ColumnInfo(name = "playground_reservation_id")
    val playgroundReservationId: Int,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: Int,
    @ColumnInfo(name = "equipment_name")
    val equipmentName: String,
    @ColumnInfo(name = "selected_quantity")
    var selectedQuantity: Int,
    @ColumnInfo(name = "unit_price")
    val unitPrice: Float,
    @ColumnInfo(name = "total_price")
    val totalPrice: Float
)

/* add/edit reservation */

data class NewReservation(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val playgroundId: Int,
    val playgroundName: String,
    val playgroundPricePerHour: Float,
    val sportId: Int,
    val sportName: String,
    val sportCenterId: Int,
    val sportCenterName: String,
    val selectedEquipments: List<NewReservationEquipment>
)

data class NewReservationEquipment(
    val equipmentId: Int,
    val equipmentName: String,
    val selectedQuantity: Int,
    val unitPrice: Float
)

