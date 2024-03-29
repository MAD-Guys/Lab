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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.view.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.application_utilities.showToasty
import it.polito.mad.sportapp.show_reservations.ShowReservationsViewModel
import it.polito.mad.sportapp.show_reservations.calendar_view.events_recycler_view.EventsAdapter

@AndroidEntryPoint
class ShowReservationsFragment : Fragment(R.layout.fragment_show_reservations) {

    internal val eventsAdapter = EventsAdapter()

    internal lateinit var recyclerView: RecyclerView

    // fragment dialog
    internal lateinit var exitDialog: AlertDialog

    // calendar view
    internal lateinit var calendarView: CalendarView
    internal lateinit var legendContainer: ViewGroup
    internal lateinit var monthLabel: TextView

    // action bar
    internal var actionBar: ActionBar? = null

    // progress bar (Steph Curry GIF)
    internal lateinit var progressBar: View

    // navigation controller
    internal lateinit var navController: NavController

    // month buttons
    internal lateinit var previousMonthButton: ImageView
    internal lateinit var nextMonthButton: ImageView

    // show reservations view model
    internal lateinit var vm: ShowReservationsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // retrieve view model from activity
        vm = ViewModelProvider(requireActivity())[ShowReservationsViewModel::class.java]

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // initialize navigation controller
        navController = findNavController()

        // initialize menu
        menuInit()

        // initialize exit dialog
        exitDialogInit()

        // setup back button
        setupOnBackPressedCallback()

        // initialize month buttons
        monthButtonsInit()

        // initialize CalendarView from layout
        calendarView = requireView().findViewById(R.id.calendar_view)
        legendContainer = requireView().findViewById(R.id.legend_container)

        // initialize month label
        monthLabel = requireView().findViewById(R.id.month_label)

        calendarInit()

        // clear reservations error
        vm.clearReservationsError()

        // setup error observer
        vm.getReservationsError.observe(viewLifecycleOwner) {
            if (it != null) {
                showToasty("error", requireContext(), it.message())
            }
        }

        // initialize RecyclerView from layout
        recyclerView = requireView().findViewById(R.id.calendar_recycler_view)

        /* show progress bar */
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.apply {
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = eventsAdapter
        }

        /* bottom bar */
        setupBottomBar()
    }

}