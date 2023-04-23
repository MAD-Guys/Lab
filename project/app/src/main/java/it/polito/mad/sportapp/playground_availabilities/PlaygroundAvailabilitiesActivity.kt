package it.polito.mad.sportapp.playground_availabilities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.playground_availabilities.calendar_utils.MonthViewContainer
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.setApplicationLocale
import it.polito.mad.sportapp.show_reservations.ShowReservationsActivity
import java.time.DayOfWeek


class PlaygroundAvailabilitiesActivity : AppCompatActivity() {
    internal lateinit var calendarView: CalendarView

    // View Model
    internal val viewModel by viewModels<PlaygroundAvailabilitiesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playground_availabilities)

        // set english as default language
        setApplicationLocale(this, "en", "EN")

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

        // TODO?

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
}