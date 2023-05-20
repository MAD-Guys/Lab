package it.polito.mad.sportapp.show_reservations.calendar_view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.view.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.hideProgressBar
import it.polito.mad.sportapp.showProgressBar
import it.polito.mad.sportapp.show_reservations.ShowReservationsViewModel
import it.polito.mad.sportapp.show_reservations.calendar_view.events_recycler_view.EventsAdapter

@AndroidEntryPoint
class ShowReservationsFragment : Fragment(R.layout.fragment_show_reservations) {

    internal val eventsAdapter = EventsAdapter()

    private lateinit var recyclerView: RecyclerView

    // fragment dialog
    internal lateinit var exitDialog: AlertDialog

    // calendar view
    internal lateinit var calendarView: CalendarView
    internal lateinit var legendContainer: ViewGroup
    internal lateinit var monthLabel: TextView

    // action bar
    internal var actionBar: ActionBar? = null

    // progress bar (Steph Curry GIF)
    private lateinit var progressBar: View

    // navigation controller
    internal lateinit var navController: NavController

    // month buttons
    internal lateinit var previousMonthButton: ImageView
    internal lateinit var nextMonthButton: ImageView

    // show reservations view model
    internal val vm by activityViewModels<ShowReservationsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // initialize navigation controller
        navController = findNavController()

        // initialize menu
        menuInit()

        // initialize exit dialog
        exitDialogInit()

        // setup back button
        setupOnBackPressedCallback(navController.currentBackStackEntry!!.destination.id)

        // initialize month buttons
        monthButtonsInit()

        // initialize CalendarView from layout
        calendarView = requireView().findViewById(R.id.calendar_view)
        legendContainer = requireView().findViewById(R.id.legend_container)

        // initialize month label
        monthLabel = requireView().findViewById(R.id.month_label)

        calendarInit()

        // initialize RecyclerView from layout
        recyclerView = requireView().findViewById(R.id.calendar_recycler_view)

        /* show progress bar */
        progressBar = view.findViewById(R.id.progressBar)
        showProgressBar(progressBar, recyclerView)

        recyclerView.apply {
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = eventsAdapter
        }

        /* bottom bar */
        setupBottomBar()

        requireView().viewTreeObserver?.addOnGlobalLayoutListener {

            if(vm.userEvents.value?.isNotEmpty() == true)
            // task completed: hide progress bar
                hideProgressBar(progressBar, recyclerView)
        }
    }

    override fun onResume() {
        super.onResume()

        // update events list
        // the invocation is in the onResume method because the list of events
        // should be refreshed each time this activity is resumed
        vm.loadEventsFromDb()
    }

}