package it.polito.mad.sportapp.playground_availabilities.calendar_utils

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.ViewContainer
import it.polito.mad.sportapp.R
import java.time.LocalDate

class DayViewContainer(
    view: View,
    selectedDateLiveData: LiveData<LocalDate>,
    setSelectedDate: (LocalDate?) -> Unit
) : ViewContainer(view) {
    internal val dayContainer = view as RelativeLayout
    internal val dayText = view.findViewById<TextView>(R.id.calendar_day_text)
    internal lateinit var day : CalendarDay

    init {
        dayContainer.setOnClickListener {
            // check the day position as we do not want to select in or out dates
            if (day.position != DayPosition.MonthDate) return@setOnClickListener

            // set the clicked day as the selected one, unless it was already selected
            setSelectedDate(
                if (day.date == selectedDateLiveData.value) null    // unselect date
                else day.date                                       // select this date
            )
        }
    }
}