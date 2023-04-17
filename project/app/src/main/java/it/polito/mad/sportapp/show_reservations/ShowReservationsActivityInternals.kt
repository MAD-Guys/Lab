package it.polito.mad.sportapp.show_reservations

import android.annotation.SuppressLint
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.dpToPx
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

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

            // set visibility of day text view as visible
            dayTextView.visibility = View.VISIBLE

            val eventBottomView = container.eventBottomView
            eventBottomView.background = null

            container.day = data
            dayTextView.text = data.date.dayOfMonth.toString()

            if (container.day.position == DayPosition.MonthDate) {

                val events = events[container.day.date]

                // create views and display events tags if any
                events?.let {
                    if (it.size == 1) {
                        eventBottomView.setBackgroundColor(getColor(events[0].color))
                    } else {
                        eventBottomView.setBackgroundColor(getColor(it.last().color))

                        var idToAnchor: Int = eventBottomView.id

                        //create events tags
                        for (i in it.size - 2 downTo 0) {
                            val sportTag = inflateDateSportTag(
                                idToAnchor,
                                it[i].color
                            )

                            // set the new anchor id
                            idToAnchor = sportTag.id

                            // add tag to the day relative layout
                            dayRelativeLayout.addView(sportTag)
                        }
                    }
                }

                // set background color for in dates
                dayRelativeLayout.setBackgroundColor(getColor(R.color.white))

                // mark current date
                if (container.day.date == LocalDate.now()) {
                    dayTextView.setTextColor(getColor(R.color.blue_500))
                    dayRelativeLayout.setBackgroundResource(R.drawable.current_day_selected_bg)
                } else {
                    dayTextView.setTextColor(getColor(R.color.black))
                }

                // mark selected date
                if (container.day.date == vm.selectedDate.value) {
                    dayTextView.setTextColor(getColor(R.color.red))
                    dayRelativeLayout.setBackgroundResource(R.drawable.day_selected_bg)
                    updateAdapterForDate(container.day.date)
                }
            }
            // set background color for out dates
            else {
                dayRelativeLayout.setBackgroundColor(getColor(R.color.out_dates_color))
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

internal fun ShowReservationsActivity.inflateDateSportTag(
    anchorPointId: Int,
    @ColorRes color: Int
): View {

    // create the new view
    val sportTag = View(this)

    // set the id of the new view
    sportTag.id = View.generateViewId()
    val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dpToPx(this,2.0f))

    // set the anchor point of the new view
    layoutParams.addRule(RelativeLayout.ABOVE, anchorPointId)

    // set margin bottom of the new view
    layoutParams.bottomMargin = dpToPx(this, 2.0f)

    sportTag.layoutParams = layoutParams

    // set the background color of the new view
    sportTag.setBackgroundColor(getColor(color))

    return sportTag
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