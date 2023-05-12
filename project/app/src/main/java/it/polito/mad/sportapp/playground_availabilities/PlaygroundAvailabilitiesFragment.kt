package it.polito.mad.sportapp.playground_availabilities

import android.os.Bundle
import android.view.MenuItem
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
import it.polito.mad.sportapp.reservation_management.ReservationManagementViewModel
import java.time.LocalDateTime


@AndroidEntryPoint
class PlaygroundAvailabilitiesFragment : Fragment(R.layout.playground_availabilities_view) {
    // View Models
    internal val reservationVM: ReservationManagementViewModel by viewModels()
    internal val playgroundsVM: PlaygroundAvailabilitiesViewModel by viewModels()

    // menu icons
    internal var addReservationButton: MenuItem? = null
    internal var addReservationSlotButton: MenuItem? = null

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
        reservationVM.reservationManagementMode = ReservationManagementMode.from(
            arguments?.getString("mode")
        )

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

        // * Manage add/edit mode *

        /* (if necessary) switch to add/edit mode */
        reservationVM.reservationManagementMode?.let { _ ->
            // retrieve the reservation data to edit
            arguments?.getBundle("reservation")?.let {
                reservationVM.setReservationBundle(it)
            }

            reservationVM.reservationBundle.observe(viewLifecycleOwner) { newBundle ->
                // change selected date
                newBundle.getString("start_time")?.let {
                    val startTime = LocalDateTime.parse(it)
                    playgroundsVM.setSelectedDate(startTime.toLocalDate())

                    playgroundAvailabilitiesAdapter.reservationBundle = newBundle
                    playgroundAvailabilitiesAdapter.notifyDataSetChanged()
                }
            }

            this.switchToAddOrEditMode()
        }
    }
}