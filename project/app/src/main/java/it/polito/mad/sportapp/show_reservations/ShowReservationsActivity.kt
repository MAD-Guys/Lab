package it.polito.mad.sportapp.show_reservations

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.stacktips.view.CalendarListener
import com.stacktips.view.CustomCalendarView
import com.stacktips.view.DayDecorator
import es.dmoral.toasty.Toasty
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.showToasty
import java.text.SimpleDateFormat
import java.util.*

class ShowReservationsActivity : AppCompatActivity() {

    // calendar view
    private lateinit var calendarView: CustomCalendarView
    private lateinit var currentCalendar: Calendar

    // show reservations view model
    private val vm by viewModels<ShowReservationsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_reservations)

        // configure toasts appearance
        Toasty.Config.getInstance()
            .allowQueue(true) // optional (prevents several Toastys from queuing)
            .setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100) // optional (set toast gravity, offsets are optional)
            .supportDarkTheme(true) // optional (whether to support dark theme or not)
            .setRTL(true) // optional (icon is on the right)
            .apply() // required

        // initialize CustomCalendarView from layout
        calendarView = findViewById(R.id.calendar_view)

        calendarInit()

    }

    // initialize calendar information
    private fun calendarInit() {

        // initialize calendar with date
        currentCalendar = Calendar.getInstance(Locale.getDefault())

        // show Monday as first date of week
        calendarView.firstDayOfWeek = Calendar.MONDAY

        // show / hide overflow days of a month
        calendarView.setShowOverflowDate(false)

        // call refreshCalendar to update calendar
        calendarView.refreshCalendar(currentCalendar)

        // handle custom calendar events
        calendarView.setCalendarListener(object : CalendarListener {

            override fun onDateSelected(date: Date) {
                val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                showToasty("info", this@ShowReservationsActivity, df.format(date).toString())
            }

            override fun onMonthChanged(date: Date) {
                val df = SimpleDateFormat("MM-yyyy", Locale.getDefault())
                showToasty("info", this@ShowReservationsActivity, df.format(date).toString())
            }
        })

        // set custom font
        val typeface = ResourcesCompat.getFont(this, R.font.poppins_medium)!!
        calendarView.customTypeface = typeface
        calendarView.refreshCalendar(currentCalendar)
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