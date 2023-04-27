package it.polito.mad.sportapp.events_list_view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.events_list_view.events_list_recycler_view.EventsListAdapter
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.playground_availabilities.PlaygroundAvailabilitiesActivity
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.generateEvents
import java.time.LocalDate

@AndroidEntryPoint
class EventsListViewActivity : AppCompatActivity() {

    private val eventsListAdapter = EventsListAdapter()

    private lateinit var userEvents: Map<LocalDate, List<DetailedReservation>>

    // generate events
    internal val events = generateEvents().sortedBy {
        it.time
    }.groupBy {
        it.time.toLocalDate()
    }

    private lateinit var eventsListView: RecyclerView

    // events view model
    private val vm by viewModels<EventsListViewModel>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_list_view)

        val currentDate = LocalDate.now()

        // initialize RecyclerView from layout
        eventsListView = findViewById(R.id.events_list_recycler_view)

        eventsListView.apply {
            layoutManager =
                LinearLayoutManager(this@EventsListViewActivity, RecyclerView.VERTICAL, false)
            adapter = eventsListAdapter
        }

        vm.userEvents.observe(this) {
            userEvents = it
            eventsListAdapter.notifyDataSetChanged()
            scrollToCurrentDate(currentDate)
        }

        // add events to the adapter
        eventsListAdapter.events.addAll(events.values.flatten())

        scrollToCurrentDate(currentDate)

        eventsListAdapter.notifyDataSetChanged()

    }

    override fun onResume() {
        super.onResume()

        // update events list
        // the invocation is in the onResume method because the list of events
        // should be refreshed each time this activity is resumed
        vm.getUserEventsFromDb()
    }

    // scroll to the current date event
    private fun scrollToCurrentDate (date: LocalDate) {
        val indexToScroll = if (events.containsKey(date)) {
            eventsListAdapter.events.indexOf(events[date]?.first())
        } else {
            getNextEvent(date)
        }
        // scroll to current date event
        eventsListView.layoutManager?.scrollToPosition(indexToScroll)
    }

    /* app menu */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate and render the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.events_list_view_menu, menu)
        // change app bar's title
        supportActionBar?.title = "My Reservations"

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        // detect which item has been selected and perform corresponding action
        R.id.show_profile_button -> navigateTo(ShowProfileActivity::class.java)
        R.id.playground_availabilities_button -> navigateTo(PlaygroundAvailabilitiesActivity::class.java)
        R.id.show_reservations_button -> {
            this.finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // get index from the nearest current date event if the event with current day is not available
    private fun getNextEvent(currentDate: LocalDate): Int {
        val nextEvent = events.keys.find { it.isAfter(currentDate) }

        return if (nextEvent != null) {
            eventsListAdapter.events.indexOf(events[nextEvent]?.first())
        } else {
            eventsListAdapter.events.size - 1
        }
    }

}