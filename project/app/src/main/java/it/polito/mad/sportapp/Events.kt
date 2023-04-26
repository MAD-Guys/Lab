package it.polito.mad.sportapp

import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

// Event data class
data class Event(
    val id: Int,
    val time: LocalDateTime,
    val sportName: String,
    val sportDuration: String,
    val sportCenterName: String,
    val sportPlaygroundName: String
)

// generate events
fun generateEvents(): List<Event> = buildList {

    val currentMonth = YearMonth.now()

    currentMonth.minusMonths(1).atDay(11).also { date ->
        add(
            Event(
                1,
                date.atTime(14, 30),
                "Padel",
                "1h",
                "Sport Center 1",
                "Playground 1"
            )
        )
        add(
            Event(
                2,
                date.atTime(8, 30),
                "Basket",
                "30m",
                "Sport Center 2",
                "Playground 2"
            )
        )
    }

    currentMonth.minusMonths(1).atDay(13).also { date ->
        add(
            Event(
                3,
                date.atTime(19, 0),
                "Tennis",
                "1h",
                "Sport Center 3",
                "Playground 3"
            )
        )
    }

    currentMonth.atDay(7).also { date ->
        add(
            Event(
                4,
                date.atTime(15, 30),
                "Padel",
                "1h 30m",
                "Sport Center 3",
                "Playground 1"
            )
        )
    }

    currentMonth.atDay(11).also { date ->
        add(
            Event(
                5,
                date.atTime(14, 30),
                "Padel",
                "2h",
                "Sport Center 2",
                "Playground 7"
            )
        )

        add(
            Event(
                6,
                date.atTime(9, 30),
                "Basket",
                "1h 30m",
                "Sport Center 1",
                "Playground 3"
            )
        )
    }

    currentMonth.atDay(17).also { date ->
        add(
            Event(
                7,
                date.atTime(13, 15),
                "Beach Volley",
                "45m",
                "Sport Center 1",
                "Playground 2"
            )
        )
    }

    currentMonth.atDay(20).also { date ->
        add(
            Event(
                8,
                date.atTime(17, 0),
                "11-a-side Soccer",
                "30m",
                "Sport Center 2",
                "Playground 4"
            )
        )
        add(
            Event(
                9,
                date.atTime(21, 30),
                "Basket",
                "1h 30m",
                "Sport Center 4",
                "Playground 5"
            )
        )
        add(
            Event(
                10,
                date.atTime(9, 45),
                "Tennis",
                "1h 45m",
                "Sport Center 3",
                "Playground 6"
            )
        )
    }

    currentMonth.atDay(22).also { date ->
        add(
            Event(
                11,
                date.atTime(11, 15),
                "Table Tennis",
                "45m",
                "Sport Center 1",
                "Playground 1"
            )
        )
    }

    currentMonth.atDay(25).also { date ->
        add(
            Event(
                12,
                date.atTime(11, 0),
                "Volleyball",
                "1h 30m",
                "Sport Center 2",
                "Playground 2"
            )
        )
        add(
            Event(
                13,
                date.atTime(17, 40),
                "Tennis",
                "1h",
                "Sport Center 3",
                "Playground 5"
            )
        )
    }

    currentMonth.plusMonths(1).atDay(5).also { date ->
        add(
            Event(
                14,
                date.atTime(12, 0),
                "Padel",
                "1h",
                "Sport Center 1",
                "Playground 8"
            )
        )
        add(
            Event(
                15,
                date.atTime(10, 0),
                "Basket",
                "1h 30m",
                "Sport Center 2",
                "Playground 9"
            )
        )
    }

    currentMonth.plusMonths(1).atDay(16).also { date ->
        add(
            Event(
                16,
                date.atTime(19, 0),
                "5-a-side Soccer",
                "1h",
                "Sport Center 3",
                "Playground 10"
            )
        )
    }

}

// format event
val eventDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm", Locale.ENGLISH)