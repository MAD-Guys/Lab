package it.polito.mad.sportapp.show_reservations

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import it.polito.mad.sportapp.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/* Internals functions related to the Show Reservations Activity */

// initialize month buttons
internal fun ShowReservationsActivity.monthButtonsInit() {

    // initialize previous / next month buttons and their listeners
    previousMonthButton = findViewById(R.id.previous_month_button)

    previousMonthButton.setOnClickListener {
        calendarView.findFirstVisibleMonth()?.let { month ->
            val newMonth = month.yearMonth.minusMonths(1)
            handleCurrentMonthChanged(newMonth)
        }
    }

    nextMonthButton = findViewById(R.id.next_month_button)

    nextMonthButton.setOnClickListener {
        calendarView.findFirstVisibleMonth()?.let { month ->
            val newMonth = month.yearMonth.plusMonths(1)
            handleCurrentMonthChanged(newMonth)
        }
    }
}

// event adapter for date
@SuppressLint("NotifyDataSetChanged")
fun ShowReservationsActivity.updateAdapterForDate(date: LocalDate?) {
    eventsAdapter.events.clear()
    eventsAdapter.events.addAll(events[date].orEmpty())
    eventsAdapter.notifyDataSetChanged()
}

// initialize calendar information
internal fun ShowReservationsActivity.calendarInit() {

    // bind days to the calendar recycler view
    calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
        // call only when a new container is needed.
        override fun create(view: View) =
            DayViewContainer(view, vm, updater = ::updateAdapterForDate)

        // call every time we need to reuse a container.
        override fun bind(container: DayViewContainer, data: CalendarDay) {

            // day layouts and views
            val dayRelativeLayout = container.relativeLayout
            val dayTextView = container.textView
            val eventTag = container.eventTag

            // set visibility of day text view as visible
            dayTextView.visibility = View.VISIBLE

            // set visibility of event tag as gone
            eventTag.visibility = View.GONE

            container.day = data
            dayTextView.text = data.date.dayOfMonth.toString()

            val currentDate = LocalDate.now()

            if (data.position == DayPosition.MonthDate) {

                val events = events[data.date]

                // set background color and text for in dates
                dayRelativeLayout.setBackgroundColor(getColor(R.color.white))
                dayTextView.setTextColor(getColor(R.color.black))

                // mark current date
                if (data.date == currentDate) {
                    dayTextView.setTextColor(getColor(R.color.primary_orange))
                    dayRelativeLayout.setBackgroundResource(R.drawable.current_day_selected_bg)
                }

                // mark selected date
                if (data.date != currentDate && data.date == vm.selectedDate.value) {
                    dayTextView.setTextColor(getColor(R.color.blue_500))
                    dayRelativeLayout.setBackgroundResource(R.drawable.day_selected_bg)
                }

                // display events tags and events, if any
                if (events != null) {
                    eventTag.visibility = View.VISIBLE

                    if (YearMonth.from(data.date) == vm.currentMonth.value && data.date == vm.selectedDate.value) {
                        updateAdapterForDate(data.date)
                    }
                } else {
                    if (YearMonth.from(data.date) == vm.currentMonth.value && data.date == vm.selectedDate.value) {
                        updateAdapterForDate(null)
                    }
                }

            }
            // set background color and text for out dates
            else {
                dayRelativeLayout.setBackgroundColor(getColor(R.color.out_dates_color))
                dayTextView.setTextColor(getColor(R.color.event_tag_color))
            }

        }
    }

    // initialize current month live data variable
    vm.currentMonth.observe(this) { month ->
        val monthString = month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        monthLabel.text = capitalizeFirstLetter(monthString)
    }

    // initialize selected date live data variable
    vm.selectedDate.observe(this) {
        // update calendar
        calendarView.notifyCalendarChanged()
    }

    // initialize days of week to monday
    val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)

    legendContainer.children
        .map { it as TextView }
        .forEachIndexed { index, textView ->
            val dayOfWeek = daysOfWeek[index]
            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            textView.text = title
        }

    // attach month scroll listener
    calendarView.monthScrollListener = { month ->
        handleCurrentMonthChanged(month.yearMonth)
    }

    // finalize calendar view initialization
    vm.currentMonth.value?.let {
        val startMonth = it.minusMonths(100)
        val endMonth = it.plusMonths(100)

        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(it)
    }

}

// handle new selected month
internal fun ShowReservationsActivity.handleCurrentMonthChanged(month: YearMonth) {
    vm.setCurrentMonth(month)
    calendarView.smoothScrollToMonth(month)

    //update event
    updateAdapterForDate(null)

    // update calendar
    calendarView.notifyCalendarChanged()
}

// capitalize the first letter of the string
internal fun ShowReservationsActivity.capitalizeFirstLetter(string: String): String {
    return string.substring(0, 1).uppercase() + string.substring(1).lowercase()
}