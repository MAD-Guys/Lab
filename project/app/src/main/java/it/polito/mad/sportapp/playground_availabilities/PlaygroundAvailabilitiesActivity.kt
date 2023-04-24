package it.polito.mad.sportapp.playground_availabilities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.setApplicationLocale
import it.polito.mad.sportapp.show_reservations.ShowReservationsActivity
import it.polito.mad.sportapp.show_reservations.handleCurrentMonthChanged
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class PlaygroundAvailabilitiesActivity : AppCompatActivity() {
    internal lateinit var calendarView: CalendarView

    // View Model
    internal val viewModel by viewModels<PlaygroundAvailabilitiesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playground_availabilities)

        /* retrieve the calendar view */
        calendarView = findViewById(R.id.calendar_view)

        /* initialize month days */
        this.initCalendarDays()

        val daysOfWeek = daysOfWeek(firstDayOfWeek=DayOfWeek.MONDAY)

        /* setup calendar */
        viewModel.currentMonth.value?.let {
            val startMonth = it.minusMonths(100)
            val endMonth = it.plusMonths(100)

            calendarView.setup(startMonth, endMonth, daysOfWeek.first())
            calendarView.scrollToMonth(it)
        }

        /* initialize calendar header */
        this.initCalendarHeader(daysOfWeek)

        /* initialize month buttons */
        val previousMonthButton = findViewById<ImageView>(R.id.previous_month_button)
        val nextMonthButton = findViewById<ImageView>(R.id.next_month_button)

        previousMonthButton.setOnClickListener {
            viewModel.currentMonth.value?.let {
                val previousMonth = it.minusMonths(1)
                calendarView.smoothScrollToMonth(previousMonth)
            }
        }

        nextMonthButton.setOnClickListener {
            viewModel.currentMonth.value?.let {
                val nextMonth = it.plusMonths(1)
                calendarView.smoothScrollToMonth(nextMonth)
            }
        }

        calendarView.monthScrollListener = { newMonth ->
            viewModel.setCurrentMonth(newMonth.yearMonth)
        }

        /* months view model observers */
        viewModel.currentMonth.observe(this) {
            calendarView.smoothScrollToMonth(it)
        }

        /* month label changer */
        val monthLabel = findViewById<TextView>(R.id.month_label)

        viewModel.currentMonth.observe(this) {
            monthLabel.text = capitalize(it.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
        }

        /* dates view model observers */

        viewModel.selectedDate.observe(this) {
            calendarView.notifyDateChanged(it)
        }

        viewModel.previousSelectedDate.observe(this) {
            calendarView.notifyDateChanged(it)
        }



        // TODO ?

    }

    /** Create the top bar menu */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.playground_availabilities_menu, menu)

        // change app bar's title
        supportActionBar?.title = "Playground availabilities"
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        // detect user selection
        R.id.show_reservations_button -> navigateTo(ShowReservationsActivity::class.java)
        R.id.show_profile_button -> navigateTo(ShowProfileActivity::class.java)
        else -> super.onOptionsItemSelected(item)
    }

    /* utils */
    private fun capitalize(str: String): String {
        return str.substring(0,1).uppercase() + str.substring(1).lowercase()
    }
}