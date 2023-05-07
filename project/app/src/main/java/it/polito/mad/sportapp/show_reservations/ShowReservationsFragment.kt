package it.polito.mad.sportapp.show_reservations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.view.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.show_reservations.events_recycler_view.EventsAdapter

@AndroidEntryPoint
class ShowReservationsFragment : Fragment(R.layout.fragment_show_reservations) {

    internal val eventsAdapter = EventsAdapter()

    private lateinit var recyclerView: RecyclerView

    // calendar view
    internal lateinit var calendarView: CalendarView
    internal lateinit var legendContainer: ViewGroup
    internal lateinit var monthLabel: TextView

    // action bar
    internal var actionBar: ActionBar? = null

    // navigation controller
    internal lateinit var navController: NavController

    // month buttons
    internal lateinit var previousMonthButton: ImageView
    internal lateinit var nextMonthButton: ImageView

    // show reservations view model
    internal val vm by viewModels<ShowReservationsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // initialize navigation controller
        navController = findNavController()

        // initialize menu
        menuInit()

        monthButtonsInit()

        // initialize CalendarView from layout
        calendarView = requireView().findViewById(R.id.calendar_view)
        legendContainer = requireView().findViewById(R.id.legend_container)

        // initialize month label
        monthLabel = requireView().findViewById(R.id.month_label)

        calendarInit()

        // initialize RecyclerView from layout
        recyclerView = requireView().findViewById(R.id.calendar_recycler_view)

        recyclerView.apply {
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = eventsAdapter
        }

        // get bottom navigation bar
        val bottomNavigationBar: View =
            (requireActivity() as AppCompatActivity).findViewById(R.id.bottom_navigation_bar)

        // show bottom navigation bar
        bottomNavigationBar.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        // update events list
        // the invocation is in the onResume method because the list of events
        // should be refreshed each time this activity is resumed
        vm.loadEventsFromDb()
    }

}