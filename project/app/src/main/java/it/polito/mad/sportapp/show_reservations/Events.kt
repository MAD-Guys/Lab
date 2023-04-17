package it.polito.mad.sportapp.show_reservations

import androidx.annotation.ColorRes
import it.polito.mad.sportapp.R
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// Event data class
data class Event(
    val time: LocalDateTime,
    val sportName: String,
    val sportDuration: String,
    @ColorRes val color: Int
)

// generate events
fun generateEvents(): List<Event> = buildList {

    val currentMonth = YearMonth.now()

    currentMonth.atDay(19).also { date ->
        add(
            Event(
                date.atTime(17, 0),
                "11-a-side Soccer",
                "30 minutes",
                R.color.blue_200,
            )
        )
        add(
            Event(
                date.atTime(21, 30),
                "Basket",
                "1 hour 30 minutes",
                R.color.red,
            )
        )
        add(
            Event(
                date.atTime(9, 45),
                "Tennis",
                "1 hour 45 minutes",
                R.color.green,
            )
        )
    }

    currentMonth.atDay(25).also { date ->
        add(
            Event(
                date.atTime(11, 0),
                "Volleyball",
                "1 hour 30 minutes",
                R.color.primary_orange,
            )
        )
        add(
            Event(
                date.atTime(17, 40),
                "Tennis",
                "1 hour",
                R.color.green,
            )
        )
    }

    currentMonth.atDay(7).also { date ->
        add(
            Event(
                date.atTime(15, 30),
                "Padel",
                "1 hour 30 minutes",
                R.color.dark_green,
            )
        )
    }

    currentMonth.atDay(17).also { date ->
        add(
            Event(
                date.atTime(13, 20),
                "Beach Volley",
                "45 minutes",
                R.color.blue_500,
            )
        )
    }

    currentMonth.atDay(22).also { date ->
        add(
            Event(
                date.atTime(11, 15),
                "Table Tennis",
                "45 minutes",
                R.color.grey,
            )
        )
    }

    currentMonth.plusMonths(1).atDay(5).also { date ->
        add(
            Event(
                date.atTime(12, 0),
                "Padel",
                "1 hour",
                R.color.dark_green,
            )
        )
        add(
            Event(
                date.atTime(10, 0),
                "Basket",
                "1 hour 30 minutes",
                R.color.red,
            )
        )
    }

    currentMonth.plusMonths(1).atDay(16).also { date ->
        add(
            Event(
                date.atTime(19, 0),
                "5-a-side Soccer",
                "1 hour",
                R.color.purple_500,
            )
        )
    }

    currentMonth.minusMonths(1).atDay(13).also { date ->
        add(
            Event(
                date.atTime(19, 0),
                "Tennis",
                "1 hour",
                R.color.green,
            )
        )
    }

    currentMonth.minusMonths(1).atDay(11).also { date ->
        add(
            Event(
                date.atTime(14, 30),
                "Padel",
                "1 hour",
                R.color.dark_green,
            )
        )
        add(
            Event(
                date.atTime(8, 30),
                "Basket",
                "30 minutes",
                R.color.red,
            )
        )
    }

}

// format event
val eventDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")