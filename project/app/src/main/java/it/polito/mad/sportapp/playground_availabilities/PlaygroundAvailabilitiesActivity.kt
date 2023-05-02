package it.polito.mad.sportapp.playground_availabilities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import com.kizitonwose.calendar.view.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.playground_availabilities.recycler_view.PlaygroundAvailabilitiesAdapter
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.show_reservations.ShowReservationsActivity


@AndroidEntryPoint
class PlaygroundAvailabilitiesActivity : AppCompatActivity() {
    internal lateinit var calendarView: CalendarView

    // View Model
    internal val viewModel: PlaygroundAvailabilitiesViewModel by viewModels()

    // spinner adapter
    internal lateinit var selectedSportSpinner : Spinner
    internal lateinit var selectedSportSpinnerAdapter: ArrayAdapter<Sport>

    // recycler view adapter
    internal lateinit var playgroundAvailabilitiesAdapter: PlaygroundAvailabilitiesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playground_availabilities)

        /* initialize calendar view */
        this.initCalendar()

        /* initialize month buttons */
        this.initCalendarMonthButtons()

        /* selected sport spinner */
        this.initSelectedSportSpinner()

        /* change month and change selected date observers */
        this.initMonthAndDateObservers()

        /* playground availabilities observer to change dates' colors */
        this.initAvailablePlaygroundsObserver()

        // selected sport observers -> change playgrounds shown
        this.initSelectedSportObservers()

        /* playgrounds availabilities recycler view */
        this.setupAvailablePlaygroundsRecyclerView()
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