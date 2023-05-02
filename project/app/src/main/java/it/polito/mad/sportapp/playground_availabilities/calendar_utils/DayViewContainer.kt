package it.polito.mad.sportapp.playground_availabilities.calendar_utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.ViewContainer
import it.polito.mad.sportapp.R
import java.time.LocalDate

class DayViewContainer(
    private val context: Context,
    view: View,
    selectedDateLiveData: LiveData<LocalDate>,
    setSelectedDate: (LocalDate?) -> Unit
) : ViewContainer(view)
{
    private val daySquare = view as RelativeLayout
    private val dayText = view.findViewById<TextView>(R.id.calendar_day_text)
    private lateinit var day : CalendarDay
    private val availabilityTag = view.findViewById<ImageView>(R.id.availability_tag)

    init {
        daySquare.setOnClickListener {
            // check the day position as we do *not* want to select in or out dates
            if (day.position != DayPosition.MonthDate) return@setOnClickListener

            // check if it was already the selected date
            if (day.date == selectedDateLiveData.value) {
                setSelectedDate(null)       // unselect date
            }
            else {
                setSelectedDate(day.date)   // set as new selected date
            }
        }
    }

    fun setDay(day: CalendarDay) {
        this.day = day
        dayText.text = day.date.dayOfMonth.toString()
    }

    fun setAsInOrOutDate() {
        daySquare.setBackgroundColor(context.getColor(R.color.out_date_background_color))
        dayText.setTextColor(context.getColor(R.color.out_date_text_color))
        availabilityTag.visibility = ImageView.GONE
    }

    fun setAsCurrentDate() {
        daySquare.setBackgroundResource(R.drawable.current_day_selected_bg)
        dayText.setTextColor(context.getColor(R.color.current_date_text_color))
    }

    fun setAsSelectedDate() {
        daySquare.setBackgroundResource(R.drawable.day_selected_bg)
        dayText.setTextColor(context.getColor(R.color.selected_date_text_color))
    }

    fun setAsUnselectedDate() {
        daySquare.setBackgroundColor(context.getColor(R.color.month_date_background))
        dayText.setTextColor(context.getColor(R.color.month_date_text_color))
    }

    fun setAvailabilityTagColor(color: Int) {
        availabilityTag.visibility = ImageView.VISIBLE
        availabilityTag.setColorFilter(color)
    }

    fun hideTag() {
        availabilityTag.visibility = ImageView.GONE
    }
}