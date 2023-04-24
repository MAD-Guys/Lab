package it.polito.mad.sportapp.playground_availabilities

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.playground_availabilities.calendar_utils.DayViewContainer
import it.polito.mad.sportapp.playground_availabilities.calendar_utils.MonthViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


internal fun PlaygroundAvailabilitiesActivity.initCalendarHeader(daysOfWeek: List<DayOfWeek>) {
    /* initialize calendar header with days of week */
    calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
        override fun create(view: View) = MonthViewContainer(view as ViewGroup)

        override fun bind(container: MonthViewContainer, data: CalendarMonth) {
            // execute this just once
            if (container.viewGroup.tag == null) {
                container.viewGroup.tag = data.yearMonth

                container.viewGroup.children.zip(
                    daysOfWeek.asSequence()).forEach() { (dayTextView, dayOfWeek) ->
                        val weekDayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        (dayTextView as TextView).text = weekDayName
                }
            }
        }
    }
}

internal fun PlaygroundAvailabilitiesActivity.initCalendarDays() {
    calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
        override fun create(view: View) =
            DayViewContainer(this@initCalendarDays, view,
                viewModel.selectedDate, viewModel::setSelectedDate)

        override fun bind(container: DayViewContainer, data: CalendarDay) {
            // set the date in the container
            container.setDay(data)

            if (data.position != DayPosition.MonthDate) {
                container.setAsInOrOutDate()
            }
            else { // month date
                val today = LocalDate.now()

                when (data.date) {
                    today -> container.setAsCurrentDate()
                    viewModel.selectedDate.value -> container.setAsSelectedDate()
                    else -> container.setAsUnselectedDate()
                }
            }
        }
    }
}
