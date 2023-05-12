package it.polito.mad.sportapp.playground_availabilities

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.playground_availabilities.calendar_utils.CalendarDayBinder
import it.polito.mad.sportapp.playground_availabilities.calendar_utils.MonthViewContainer
import it.polito.mad.sportapp.playground_availabilities.recycler_view.PlaygroundAvailabilitiesAdapter
import it.polito.mad.sportapp.showToasty
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min

/* action bar and menu init */
internal fun PlaygroundAvailabilitiesFragment.initAppBar() {
    // retrieve activity action bar
    val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

    actionBar?.let {
        // set title
        it.setDisplayHomeAsUpEnabled(false)
        it.title = "Playground Availabilities"
    }
}

internal fun PlaygroundAvailabilitiesFragment.initMenu() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(
                R.menu.playgrounds_availabilities_menu,
                menu
            )

            // save buttons views
            addReservationButton = menu.findItem(R.id.add_reservation_button)
            addReservationSlotButton = menu.findItem(R.id.add_reservation_slot_button)

            if (reservationVM.reservationManagementMode == null) {
                // Playgrounds availabilities view -> show plus button
                addReservationButton!!.isVisible = true
            }
            else {
                // add/edit mode -> show add slot button
                addReservationSlotButton!!.isVisible = true

                if (!reservationVM.isStartTimeSet() || !reservationVM.isEndTimeSet())
                    addReservationSlotButton?.icon = AppCompatResources.getDrawable(requireContext(),
                        R.drawable.baseline_more_time_24_blurred)
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
            R.id.add_reservation_button -> {
                navigateToAddReservation()
                true
            }
            R.id.add_reservation_slot_button -> {
                if(reservationVM.isStartTimeSet() && reservationVM.isEndTimeSet())
                    navigateToManageEquipments()
                true
            }
            else -> false
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

private fun PlaygroundAvailabilitiesFragment.navigateToAddReservation() {
    // go to add/edit mode
    findNavController().navigate(R.id.action_playgroundAvailabilitiesFragment_self)
}
private fun PlaygroundAvailabilitiesFragment.navigateToManageEquipments() {
    // TODO
    showToasty("info", requireContext(), "Go to edit/add equipments")
}


/* calendar init functions */
internal fun PlaygroundAvailabilitiesFragment.initCalendar() {
    /* retrieve the calendar view */
    calendarView = requireView().findViewById(R.id.calendar_view)

    val daysOfWeek = daysOfWeek(firstDayOfWeek=DayOfWeek.MONDAY)

    /* initialize calendar header */
    this.initCalendarHeader(daysOfWeek)

    /* initialize month days */
    this.initCalendarDays()

    /* setup calendar */
    playgroundsVM.currentMonth.value?.let {
        val startMonth = it.minusMonths(100)
        val endMonth = it.plusMonths(100)

        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(it)
    }
}

private fun PlaygroundAvailabilitiesFragment.initCalendarHeader(daysOfWeek: List<DayOfWeek>) {
    /* initialize calendar header with days of week */
    calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
        override fun create(view: View) = MonthViewContainer(view as ViewGroup)

        override fun bind(container: MonthViewContainer, data: CalendarMonth) {
            // execute this just once
            if (container.viewGroup.tag == null) {
                container.viewGroup.tag = data.yearMonth

                container.viewGroup.children.zip(
                    daysOfWeek.asSequence()).forEach { (dayTextView, dayOfWeek) ->
                    val weekDayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    (dayTextView as TextView).text = weekDayName
                }
            }
        }
    }
}

private fun PlaygroundAvailabilitiesFragment.initCalendarDays() {
    // create and initialize day binder
    val calendarDayBinder = CalendarDayBinder(
        requireContext(),
        playgroundsVM.selectedDate,
        playgroundsVM::setSelectedDate,
        this::getAvailabilityPercentageOf,
        playgroundsVM::isAvailablePlaygroundsLoaded
    )

    // attach day binder to the calendar view
    calendarView.dayBinder = calendarDayBinder
}

internal fun PlaygroundAvailabilitiesFragment.initCalendarMonthButtons() {
    val previousMonthButton = requireView().findViewById<ImageView>(R.id.previous_month_button)
    val nextMonthButton = requireView().findViewById<ImageView>(R.id.next_month_button)

    previousMonthButton.setOnClickListener {
        playgroundsVM.currentMonth.value?.let {
            val previousMonth = it.minusMonths(1)
            calendarView.smoothScrollToMonth(previousMonth)
        }
    }

    nextMonthButton.setOnClickListener {
        playgroundsVM.currentMonth.value?.let {
            val nextMonth = it.plusMonths(1)
            calendarView.smoothScrollToMonth(nextMonth)
        }
    }

    // change selected month if user swipes to another month
    calendarView.monthScrollListener = { newMonth ->
        playgroundsVM.setCurrentMonth(newMonth.yearMonth)
    }
}

/* selected sport spinner */
internal fun PlaygroundAvailabilitiesFragment.initSelectedSportSpinner() {
    // create the spinner adapter
    selectedSportSpinnerAdapter = ArrayAdapter(
        requireContext(),
        android.R.layout.simple_spinner_item,
        playgroundsVM.sports.value ?: mutableListOf()
    ).also {
        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    // mount the adapter in the selected sport spinner
    selectedSportSpinner = requireView().findViewById(R.id.selected_sport_spinner)
    selectedSportSpinner.adapter = selectedSportSpinnerAdapter

    // specify the spinner callback to call at each user selection
    selectedSportSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            // retrieve selected option
            val justSelectedSport = selectedSportSpinnerAdapter.getItem(position)
            // update selected sport
            playgroundsVM.setSelectedSport(justSelectedSport)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            playgroundsVM.setSelectedSport(null)
        }
    }
}

/* observers init functions */
internal fun PlaygroundAvailabilitiesFragment.initMonthAndDateObservers() {
    /* months view model observers */
    val monthLabel = requireView().findViewById<TextView>(R.id.month_label)

    playgroundsVM.currentMonth.observe(this) { newMonth ->
        // change month label
        monthLabel.text = capitalize(
            newMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)))

        // retrieve new playground availabilities for the current month and the current sport
        playgroundsVM.updatePlaygroundAvailabilitiesForCurrentMonthAndSport()
    }

    /* dates view model observers */
    val selectedDateLabel = requireView().findViewById<TextView>(R.id.selected_date_label)

    playgroundsVM.selectedDate.observe(this) {
        // update calendar selected date
        calendarView.notifyDateChanged(it)

        // update selected date label
        selectedDateLabel.text = it.format(
            DateTimeFormatter.ofPattern("EEEE, d MMMM y", Locale.ENGLISH))

        // update recycler view data
        playgroundAvailabilitiesAdapter.selectedDate = it

        // update time slots
        playgroundAvailabilitiesAdapter.smartUpdatePlaygroundAvailabilities(
            playgroundsVM.getAvailablePlaygroundsOnSelectedDate()
        )
    }

    playgroundsVM.previousSelectedDate.observe(this) {
        calendarView.notifyDateChanged(it)
    }
}

internal fun PlaygroundAvailabilitiesFragment.initAvailablePlaygroundsObserver() {
    /* playground availabilities observer to change dates' colors */
    playgroundsVM.availablePlaygroundsPerSlot.observe(this) {
        // * update calendar dates' dots *

        // update current, previous and next months
        val currentMonth = playgroundsVM.currentMonth.value ?: playgroundsVM.defaultMonth
        val previousMonth = currentMonth.minusMonths(1)
        val nextMonth = currentMonth.plusMonths(1)

        calendarView.notifyMonthChanged(currentMonth)
        calendarView.notifyMonthChanged(previousMonth)
        calendarView.notifyMonthChanged(nextMonth)

        // update recycler view time slots
        playgroundAvailabilitiesAdapter.smartUpdatePlaygroundAvailabilities(
            playgroundsVM.getAvailablePlaygroundsOnSelectedDate()
        )

        if(it.isNotEmpty())
            playgroundsVM.setAvailablePlaygroundsLoaded()
    }
}

internal fun PlaygroundAvailabilitiesFragment.initSelectedSportObservers() {
    // sports observer
    playgroundsVM.sports.observe(this) {
        // add new sports list
        try {
            selectedSportSpinnerAdapter.clear()
            selectedSportSpinnerAdapter.addAll(it)
        }
        catch (_: UnsupportedOperationException) { }
    }

    // selected sport observer
    playgroundsVM.selectedSport.observe(this) {
        // empty current playground availabilities
        playgroundsVM.emptyPlaygroundAvailabilities()

        // retrieve new playground availabilities for the current month and the current sport
        playgroundsVM.updatePlaygroundAvailabilitiesForCurrentMonthAndSport()
    }
}

/* Playground availabilities recycler view */
internal fun PlaygroundAvailabilitiesFragment.setupAvailablePlaygroundsRecyclerView() {
    val playgroundAvailabilitiesRecyclerView =
        requireView().findViewById<RecyclerView>(R.id.playground_availabilities_rv)

    // init adapter
    playgroundAvailabilitiesAdapter = PlaygroundAvailabilitiesAdapter(
        playgroundsVM.getAvailablePlaygroundsOnSelectedDate(),
        playgroundsVM.selectedDate.value ?: playgroundsVM.defaultDate,
        playgroundsVM.slotDuration,
        reservationVM.reservationManagementMode,
        reservationVM.reservationBundle.value
    ) { playgroundId ->
        val params = bundleOf(
            "id_playground" to playgroundId
        )
        // navigate to playground details view
        findNavController().navigate(R.id.action_playgroundAvailabilitiesFragment_to_PlaygroundDetailsFragment, params)
    }

    // init recycler view
    playgroundAvailabilitiesRecyclerView.apply {
        layoutManager = LinearLayoutManager(
            requireContext(), RecyclerView.VERTICAL, false)
        adapter = playgroundAvailabilitiesAdapter
    }
}

/* bottom bar */
internal fun PlaygroundAvailabilitiesFragment.setupBottomBar() {
    // show bottom bar
    val bottomBar = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)

    // show bottom bar with the right selected button
    bottomBar.visibility = View.VISIBLE
    bottomBar.menu.findItem(R.id.playgrounds).isChecked = true
}

/* add/edit mode */
internal fun PlaygroundAvailabilitiesFragment.switchToAddOrEditMode() {
    /* app bar */
    val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

    actionBar?.let {
        // show back arrow and the right title
        it.setDisplayHomeAsUpEnabled(true)
        it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        it.title = reservationVM.reservationManagementMode!!.appBarTitle
    }

    /* menu */

    // show only add slot button
    addReservationButton?.isVisible = false
    addReservationSlotButton?.isVisible = true

    /* month and date colors */

    // change current month color
    val selectedMonthBar = requireView().findViewById<View>(R.id.app_bar_layout)
    selectedMonthBar.setBackgroundResource(reservationVM.reservationManagementMode!!.variantColorId)

    // change color to the selected date bar
    val selectedDateLabelBox = requireView().findViewById<View>(R.id.selected_date_label_box)
    selectedDateLabelBox.setBackgroundResource(reservationVM.reservationManagementMode!!.variantColorId)

    // hide bottom bar
    val bottomBar = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)
    bottomBar.visibility = View.GONE

    // reset recycler view adapter flag
    playgroundAvailabilitiesAdapter.setEditableReservationSlotsAsAvailable()
}

/* utils */

/** Compute availability percentage as the percentage of slots with at least one available playground */
private fun PlaygroundAvailabilitiesFragment.getAvailabilityPercentageOf(date: LocalDate): Float {
    val availablePlaygrounds = playgroundsVM.getAvailablePlaygroundsOn(date).mapValues {
        (_, playgrounds) -> playgrounds.filter { it.available }
    }

    val slotsWithAtLeastOneAvailability = availablePlaygrounds.count { it.value.isNotEmpty() }
    val maxNumOfSlotsPerDay = 25

    return min(1.0f, slotsWithAtLeastOneAvailability.toFloat() / maxNumOfSlotsPerDay.toFloat())
}

private fun capitalize(str: String): String {
    return str.substring(0,1).uppercase() + str.substring(1).lowercase()
}
