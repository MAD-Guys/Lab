package it.polito.mad.sportapp.events_list_view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.events_list_view.events_list_recycler_view.EventsListAdapter
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.playground_availabilities.PlaygroundAvailabilitiesActivity
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.show_reservations.ShowReservationsActivity
import it.polito.mad.sportapp.show_reservations.generateEvents
import java.time.LocalDate

class EventsListViewActivity : AppCompatActivity() {

    private val eventsListAdapter = EventsListAdapter()

    // generate events
    internal val events = generateEvents().sortedBy {
        it.time
    }.groupBy {
        it.time.toLocalDate()
    }

    private lateinit var eventsListView: RecyclerView

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_list_view)

        // initialize RecyclerView from layout
        eventsListView = findViewById(R.id.events_list_recycler_view)

        eventsListView.apply {
            layoutManager =
                LinearLayoutManager(this@EventsListViewActivity, RecyclerView.VERTICAL, false)
            adapter = eventsListAdapter
        }

        // add events to adapter
        eventsListAdapter.events.addAll(events.values.flatten())

        // scroll to current date event
        eventsListView.layoutManager?.scrollToPosition(eventsListAdapter.events.indexOf(events[LocalDate.now()]?.first()))

        eventsListAdapter.notifyDataSetChanged()

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
        R.id.show_reservations_button -> navigateTo(ShowReservationsActivity::class.java)
        else -> super.onOptionsItemSelected(item)
    }

}