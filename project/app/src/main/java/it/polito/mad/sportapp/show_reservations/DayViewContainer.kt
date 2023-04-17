package it.polito.mad.sportapp.show_reservations

import android.view.View
import android.widget.TextView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.ViewContainer
import it.polito.mad.sportapp.R
import java.time.LocalDate

class DayViewContainer(view: View, vm: ShowReservationsViewModel, updater: (LocalDate?) -> Unit) : ViewContainer(view) {

    // day constraint layout
    internal val constraintLayout: View = view.findViewById(R.id.day_layout)

    // day relative layout
    internal val relativeLayout: View = view.findViewById(R.id.day_container)

    // day text view
    internal val textView: TextView = view.findViewById(R.id.calendar_day_text)

    // manage events displayed on the day
    internal val eventTopView  = view.findViewById<View>(R.id.fourth_event)
    internal val eventBottomView = view.findViewById<View>(R.id.fifth_event)

    // calendar day
    internal lateinit var day: CalendarDay

    init {
        view.setOnClickListener {

            // check the day position as we do not want to select in or out dates.
            if (day.position == DayPosition.MonthDate) {

                // keep a reference to any previous selection
                // in case we overwrite it and need to reload it.
                val currentSelection = vm.selectedDate.value

                if (currentSelection == day.date) {
                    // if the user clicks the same date, clear selection and events.
                    vm.setSelectedDate(null)
                    updater(null)
                } else {
                    vm.setSelectedDate(day.date)
                    updater(day.date)
                }
            }
        }
    }
}