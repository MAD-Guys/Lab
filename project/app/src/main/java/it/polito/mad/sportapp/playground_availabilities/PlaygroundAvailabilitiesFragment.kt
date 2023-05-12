package it.polito.mad.sportapp.playground_availabilities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.view.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.playground_availabilities.recycler_view.PlaygroundAvailabilitiesAdapter
import it.polito.mad.sportapp.reservation_management.ReservationManagementViewModel


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

    // recycler view
    internal var playgroundAvailabilitiesRecyclerView: RecyclerView? = null
    internal lateinit var playgroundAvailabilitiesAdapter: PlaygroundAvailabilitiesAdapter

    internal var sportIdToShow: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* add/edit mode setup */
        this.addOrEditModeSetup()

        playgroundsVM.loadAllSportsAsync(sportIdToShow)

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

        if (reservationVM.reservationManagementMode != null) {
            // set reservation selection observer
            reservationVM.reservationBundle.observe(viewLifecycleOwner) { newBundle ->
                // update slots selections
                playgroundAvailabilitiesAdapter.reservationBundle = newBundle

                // refresh recycler view
                playgroundAvailabilitiesAdapter.smartUpdatePlaygroundAvailabilities(
                    playgroundsVM.getAvailablePlaygroundsOnSelectedDate()
                )

                // check if at least a slot has been selected
                addReservationSlotButton?.icon = AppCompatResources.getDrawable(
                    requireContext(),
                    if(newBundle.getString("start_slot") != null)
                    // a slot has been selected -> set white icon
                        R.drawable.baseline_more_time_24
                    else // no slot is still selected -> set blurred icon
                        R.drawable.baseline_more_time_24_blurred
                )
            }

            /* (if necessary) switch to add/edit mode */
            this.switchToAddOrEditMode()
        }
    }
}