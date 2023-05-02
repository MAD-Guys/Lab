package it.polito.mad.sportapp.playground_availabilities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.playground_availabilities.recycler_view.PlaygroundAvailabilitiesAdapter
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.show_reservations.ShowReservationsActivity
import java.time.DayOfWeek
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale


@AndroidEntryPoint
class PlaygroundAvailabilitiesActivity : AppCompatActivity() {
    internal lateinit var calendarView: CalendarView

    // View Model
    internal val viewModel: PlaygroundAvailabilitiesViewModel by viewModels()

    // recycler view
    private lateinit var playgroundAvailabilitiesAdapter: PlaygroundAvailabilitiesAdapter

    @SuppressLint("NotifyDataSetChanged")
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

        // change selected month if user swipes to another month
        calendarView.monthScrollListener = { newMonth ->
            viewModel.setCurrentMonth(newMonth.yearMonth)
        }

        /* initialize selected sport spinner */
        val selectedSportSpinner = findViewById<Spinner>(R.id.selected_sport_spinner)
        // TODO

        /* months view model observers */

        val monthLabel = findViewById<TextView>(R.id.month_label)

        viewModel.currentMonth.observe(this) { newMonth ->
            // change month view
            calendarView.smoothScrollToMonth(newMonth)

            // change month label
            monthLabel.text = capitalize(
                newMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)))

            // retrieve new playground availabilities for the current month
            viewModel.updatePlaygroundAvailabilities(newMonth)
        }

        /* playground availabilities observer to change dates' colors */
        viewModel.availablePlaygroundsPerSlot.observe(this) {
            calendarView.notifyMonthChanged(viewModel.currentMonth.value ?: viewModel.defaultMonth)
        }

        /* dates view model observers */
        val selectedDateLabel = findViewById<TextView>(R.id.selected_date_label)

        viewModel.selectedDate.observe(this) {
            // update calendar selected date
            calendarView.notifyDateChanged(it)

            // update selected date label
            selectedDateLabel.text = it.format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM y", Locale.ENGLISH))

            // update recycler view data
            playgroundAvailabilitiesAdapter.selectedDate = it
            playgroundAvailabilitiesAdapter.playgroundAvailabilities =
                viewModel.getAvailablePlaygroundsOnSelectedDate()

            // update time slots view
            playgroundAvailabilitiesAdapter.notifyDataSetChanged()
        }

        viewModel.previousSelectedDate.observe(this) {
            calendarView.notifyDateChanged(it)
        }

        // set up playgrounds availabilities recycler view
        val playgroundAvailabilitiesRecyclerView = findViewById<RecyclerView>(R.id.playground_availabilities_rv)
        playgroundAvailabilitiesAdapter = PlaygroundAvailabilitiesAdapter(
            viewModel.getAvailablePlaygroundsOnSelectedDate(),
            viewModel.selectedDate.value ?: viewModel.defaultDate,
            Duration.ofMinutes(30)
        )

        playgroundAvailabilitiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@PlaygroundAvailabilitiesActivity, RecyclerView.VERTICAL, false)
            adapter = playgroundAvailabilitiesAdapter
        }
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