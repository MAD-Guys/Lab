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
import it.polito.mad.sportapp.events_list_view.events_list_recycler_view.EventsListAdapter
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.playground_availabilities.PlaygroundAvailabilitiesFragment
import it.polito.mad.sportapp.profile.ShowProfileActivity
import java.time.LocalDate

@AndroidEntryPoint
class EventsListViewActivity : AppCompatActivity() {

    private val eventsListAdapter = EventsListAdapter()

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
            eventsListAdapter.events.clear()

            // add events to the adapter
            eventsListAdapter.events.addAll(it.values.flatten())
            eventsListAdapter.notifyDataSetChanged()
            scrollToCurrentDate(currentDate)
        }

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
        val indexToScroll = if (vm.userEvents.value?.containsKey(date) == true) {
            eventsListAdapter.events.indexOf(vm.userEvents.value?.get(date)?.first())
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
        R.id.playground_availabilities_button -> navigateTo(PlaygroundAvailabilitiesFragment::class.java)
        R.id.show_reservations_button -> {
            this.finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // get index from the nearest current date event if the event with current day is not available
    private fun getNextEvent(currentDate: LocalDate): Int {
        val nextEvent = vm.userEvents.value?.keys?.find { it.isAfter(currentDate) }

        return if (nextEvent != null) {
            eventsListAdapter.events.indexOf(vm.userEvents.value?.get(nextEvent)?.first())
        } else {
            eventsListAdapter.events.size - 1
        }
    }

}