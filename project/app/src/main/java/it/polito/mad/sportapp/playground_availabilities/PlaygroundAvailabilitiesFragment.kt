package it.polito.mad.sportapp.playground_availabilities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kizitonwose.calendar.view.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.playground_availabilities.recycler_view.PlaygroundAvailabilitiesAdapter
import it.polito.mad.sportapp.reservation_management.ReservationManagementMode
import it.polito.mad.sportapp.showToasty


@AndroidEntryPoint
class PlaygroundAvailabilitiesFragment : Fragment(R.layout.playground_availabilities_view) {
    // reservation data received from previous view
    private var reservationBundle: Bundle? = null

    // View Model
    internal val viewModel: PlaygroundAvailabilitiesViewModel by viewModels()

    // calendar
    internal lateinit var calendarView: CalendarView

    // spinner adapter
    internal lateinit var selectedSportSpinner : Spinner
    internal lateinit var selectedSportSpinnerAdapter: ArrayAdapter<Sport>

    // recycler view adapter
    internal lateinit var playgroundAvailabilitiesAdapter: PlaygroundAvailabilitiesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // determine if we are in 'add mode' or in 'edit mode' (or none)
        viewModel.reservationManagementMode = ReservationManagementMode.from(
            arguments?.getString("mode")
        )

        if (viewModel.reservationManagementMode == ReservationManagementMode.EDIT_MODE)
            reservationBundle = arguments?.getBundle("reservation")

        /* init app bar and menu */
        this.initAppBar()
        this.initMenu()

        /* initialize calendar view */
        this.initCalendar()

        /* initialize month buttons */
        this.initCalendarMonthButtons()

        /* selected sport spinner */
        this.initSelectedSportSpinner()

        /* change month and change selected date observers */
        this.initMonthAndDateObservers()

        /* playground availabilities observer to change dates' colors */
        this.initAvailablePlaygroundsObserver()

        /* selected sport observers -> change playgrounds shown */
        this.initSelectedSportObservers()

        /* playgrounds availabilities recycler view */
        this.setupAvailablePlaygroundsRecyclerView()

        /* bottom bar */
        this.setupBottomBar()
    }

    override fun onResume() {
        super.onResume()

        // TODO: remove
        showToasty("info", requireContext(),
            "$arguments"
        )
    }
}