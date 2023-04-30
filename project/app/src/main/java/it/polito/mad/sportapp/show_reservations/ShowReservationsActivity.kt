package it.polito.mad.sportapp.show_reservations

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.view.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import it.polito.mad.sportapp.events_list_view.EventsListViewActivity
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.playground_availabilities.PlaygroundAvailabilitiesActivity
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.setApplicationLocale
import it.polito.mad.sportapp.show_reservations.events_recycler_view.EventsAdapter

/* Show Reservations Activity */

@AndroidEntryPoint
class ShowReservationsActivity : AppCompatActivity() {

    internal val eventsAdapter = EventsAdapter()

    private lateinit var recyclerView: RecyclerView

    // calendar view
    internal lateinit var calendarView: CalendarView
    internal lateinit var legendContainer: ViewGroup
    internal lateinit var monthLabel: TextView

    // month buttons
    internal lateinit var previousMonthButton: ImageView
    internal lateinit var nextMonthButton: ImageView

    // show reservations view model
    internal val vm by viewModels<ShowReservationsViewModel>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set content view with activity layout
        setContentView(R.layout.activity_show_reservations)

        // configure toasts appearance
        Toasty.Config.getInstance()
            .allowQueue(true) // optional (prevents several Toastys from queuing)
            .setGravity(
                Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                0,
                100
            ) // optional (set toast gravity, offsets are optional)
            .supportDarkTheme(true) // optional (whether to support dark theme or not)
            .setRTL(true) // optional (icon is on the right)
            .apply() // required

        // set english as default language
        setApplicationLocale(this, "en", "EN")

        monthButtonsInit()

        // initialize CalendarView from layout
        calendarView = findViewById(R.id.calendar_view)
        legendContainer = findViewById(R.id.legend_container)

        // initialize month label
        monthLabel = findViewById(R.id.month_label)

        calendarInit()

        // initialize RecyclerView from layout
        recyclerView = findViewById(R.id.calendar_recycler_view)

        recyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@ShowReservationsActivity, RecyclerView.VERTICAL, false)
            adapter = eventsAdapter
        }

    }

    override fun onResume() {
        super.onResume()

        // update events list
        // the invocation is in the onResume method because the list of events
        // should be refreshed each time this activity is resumed
        vm.loadEventsFromDb()
    }

    /* app menu */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate and render the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.show_reservations_menu, menu)
        // change app bar's title
        supportActionBar?.title = "My Reservations"

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        // detect which item has been selected and perform corresponding action
        R.id.show_profile_button -> navigateTo(ShowProfileActivity::class.java)
        R.id.playground_availabilities_button -> navigateTo(PlaygroundAvailabilitiesActivity::class.java)
        R.id.list_view_button -> navigateTo(EventsListViewActivity::class.java)
        else -> super.onOptionsItemSelected(item)
    }
}