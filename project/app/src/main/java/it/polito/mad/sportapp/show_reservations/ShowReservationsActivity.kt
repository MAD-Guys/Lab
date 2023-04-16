package it.polito.mad.sportapp.show_reservations

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kizitonwose.calendar.view.CalendarView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.profile.ShowProfileActivity

class ShowReservationsActivity : AppCompatActivity() {

    // calendar view
    internal lateinit var calendarView: CalendarView
    internal lateinit var legendContainer: ViewGroup
    internal lateinit var monthLabel: TextView

    // month buttons
    internal lateinit var previousMonthButton: ImageView
    internal lateinit var nextMonthButton: ImageView

    // show reservations view model
    internal val vm by viewModels<ShowReservationsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_reservations)

        monthButtonsInit()

        // initialize CalendarView from layout
        calendarView = findViewById(R.id.calendar_view)
        legendContainer = findViewById(R.id.legend_container)

        // initialize month label
        monthLabel = findViewById(R.id.month_label)

        calendarInit()

    }

    /* app menu */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate and render the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.show_reservations_menu, menu)
        // change app bar's title
        supportActionBar?.title = "Dashboard"

        return true
    }

    //TODO: create a bottom bar in order to delete the button
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        // detect which item has been selected and perform corresponding action
        R.id.show_profile_button -> handleShowProfileButton()
        else -> super.onOptionsItemSelected(item)
    }

    private fun handleShowProfileButton(): Boolean {
        val showProfileIntent = Intent(this, ShowProfileActivity::class.java)
        startActivity(showProfileIntent)

        return true
    }

}