package it.polito.mad.sportapp.playground_availabilities.calendar_utils

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.LocalDate

class CalendarDayBinder(
    private val context: Context,
    private val selectedDate: LiveData<LocalDate>,
    private val setSelectedDate: (LocalDate?) -> Unit,
    private val getAvailabilityPercentageOf: (LocalDate) -> Float,
    private val isAvailablePlaygroundsLoaded: () -> Boolean
) : MonthDayBinder<DayViewContainer> {

    override fun create(view: View) =
        DayViewContainer(context, view, selectedDate, setSelectedDate)

    override fun bind(container: DayViewContainer, data: CalendarDay) {
        // set the date in the container
        container.setDay(data)

        // hide tag
        container.hideTag()

        if (data.position != DayPosition.MonthDate) {
            container.setAsInOrOutDate()
        }
        else { // month date
            val today = LocalDate.now()

            when (data.date) {
                today -> container.setAsCurrentDate()
                (selectedDate.value ?: LocalDate.now()) -> container.setAsSelectedDate()
                else -> container.setAsUnselectedDate()
            }

            if (data.date != today && data.date != selectedDate.value) {
                // compute the availability percentage on that date to color it properly
                val availabilityPercentage = getAvailabilityPercentageOf(data.date)

                // if this day is completely full (no existing available playgrounds), show tag
                if (isAvailablePlaygroundsLoaded() && availabilityPercentage == 0.0f) {
                    container.showTag()
                }
            }
        }
    }
}