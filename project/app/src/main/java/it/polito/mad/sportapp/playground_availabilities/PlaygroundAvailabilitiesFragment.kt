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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kizitonwose.calendar.view.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.room.RoomSport
import it.polito.mad.sportapp.application_utilities.hideProgressBar
import it.polito.mad.sportapp.playground_availabilities.recycler_view.PlaygroundAvailabilitiesAdapter
import it.polito.mad.sportapp.application_utilities.showProgressBar
import it.polito.mad.sportapp.reservation_management.ReservationSlotSelectionViewModel



@AndroidEntryPoint
class PlaygroundAvailabilitiesFragment : Fragment(R.layout.playground_availabilities_view) {
    // View Models
    internal val reservationVM: ReservationSlotSelectionViewModel by viewModels()
    internal val playgroundsVM: PlaygroundAvailabilitiesViewModel by viewModels()

    // menu icons
    internal var addReservationButton: MenuItem? = null
    internal var addReservationSlotButton: MenuItem? = null

    // floating action button
    internal lateinit var floatingButton: FloatingActionButton

    // calendar
    internal lateinit var calendarView: CalendarView

    // spinner adapter
    internal lateinit var selectedSportSpinner : Spinner
    internal lateinit var selectedSportSpinnerAdapter: ArrayAdapter<RoomSport>

    // recycler view
    internal var playgroundAvailabilitiesRecyclerView: RecyclerView? = null
    internal lateinit var playgroundAvailabilitiesAdapter: PlaygroundAvailabilitiesAdapter

    // progress bar (Steph Curry GIF)
    private lateinit var progressBar: View
    private lateinit var slotAvailabilitiesSection: View

    internal var sportIdToShow: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* show progress bar */
        slotAvailabilitiesSection = view.findViewById(R.id.slot_availabilities_section)
        progressBar = view.findViewById(R.id.progressBar)
        showProgressBar(progressBar, slotAvailabilitiesSection)

        /* add/edit mode setup */
        this.manageAddOrEditModeParams()

        playgroundsVM.loadAllSportsAsync(sportIdToShow)

        /* init app bar and menu */
        this.initAppBar()
        this.initMenu()

        /* init floating action button */
        this.initFloatingButton()

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

        // set reservation selection observer
        reservationVM.reservationBundle.observe(viewLifecycleOwner) { newBundle ->
            // update slots selections
            playgroundAvailabilitiesAdapter.reservationBundle = newBundle

            // refresh recycler view
            playgroundAvailabilitiesAdapter.smartUpdatePlaygroundAvailabilities(
                playgroundsVM.getAvailablePlaygroundsOnSelectedDate()
            )

            // check if at least a slot has been selected
            if(newBundle.getString("start_slot") != null) {
                // * a slot has been selected *

                // set white icon
                addReservationSlotButton?.icon = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.baseline_more_time_24
                )

                // show floating button
                floatingButton.visibility = View.VISIBLE
            }
            else {
                // no slot is still selected -> set blurred icon
                addReservationSlotButton?.icon = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.baseline_more_time_24_blurred
                )
            }

        }

        /* (if necessary) switch to add/edit mode */
        if (reservationVM.reservationManagementModeWrapper.mode != null) {
            this.setupAddOrEditModeView()
        }

        requireView().viewTreeObserver?.addOnGlobalLayoutListener {

            if(playgroundsVM.availablePlaygroundsPerSlot.value?.isNotEmpty() == true)
                // task completed: hide progress bar
                hideProgressBar(progressBar, slotAvailabilitiesSection)
        }
    }

    override fun onPause() {
        super.onPause()

        // to not show red dot temporary state when coming back to this view:
        // clear available playgrounds data (and unset flag)
        playgroundsVM.clearAvailablePlaygroundsPerSlot()
    }
}