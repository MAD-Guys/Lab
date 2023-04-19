package it.polito.mad.sportapp.show_reservations

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.view.CalendarView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.setApplicationLocale
import it.polito.mad.sportapp.show_reservations.events_recycler_view.EventsAdapter

/* Show Reservations Activity */

class ShowReservationsActivity : AppCompatActivity() {

    internal val eventsAdapter = EventsAdapter()

    // generate events
    internal val events = generateEvents().sortedBy {
        it.time
    }.groupBy {
        it.time.toLocalDate()
    }

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
        setContentView(R.layout.activity_show_reservations)

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

        eventsAdapter.notifyDataSetChanged()

    }

    /* app menu */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate and render the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.show_reservations_menu, menu)
        // change app bar's title
        supportActionBar?.title = "Reservations"

        return true
    }

    //TODO: create a bottom bar in order to delete the button
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        // detect which item has been selected and perform corresponding action
        R.id.show_profile_button -> handleShowProfileButton()
        R.id.book_playground_button -> {
            Log.d("ShowReservationsActivity", "Book playground button clicked! (not implemented yet)")

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun handleShowProfileButton(): Boolean {
        val showProfileIntent = Intent(this, ShowProfileActivity::class.java)
        startActivity(showProfileIntent)

        return true
    }

}