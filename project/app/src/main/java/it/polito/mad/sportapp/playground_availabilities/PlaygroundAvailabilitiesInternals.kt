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
import es.dmoral.toasty.Toasty
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.playground_availabilities.calendar_utils.CalendarDayBinder
import it.polito.mad.sportapp.playground_availabilities.calendar_utils.MonthViewContainer
import it.polito.mad.sportapp.playground_availabilities.recycler_view.PlaygroundAvailabilitiesAdapter
import it.polito.mad.sportapp.reservation_management.ReservationManagementMode
import it.polito.mad.sportapp.application_utilities.showToasty
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min

/* add/edit mode setup */
internal fun PlaygroundAvailabilitiesFragment.manageAddOrEditModeParams() {
    if(reservationVM.reservationManagementModeWrapper.mode == null && arguments?.getString("mode") != null) {
        // NOT coming from a next view (with back arrow navigation)
        reservationVM.reservationManagementModeWrapper.mode = ReservationManagementMode.from(
            arguments?.getString("mode")!!
        )
    }

    // determine if we are in 'add mode' or in 'edit mode' (or none)
    reservationVM.reservationManagementModeWrapper.mode?.also { mode ->
        // * if so, save the received reservation bundle  *
        arguments?.getBundle("reservation")?.let { bundle ->
            if (reservationVM.reservationBundle.value!!.getString("start_slot") == null) {
                // NOT coming from a next view (with back arrow navigation)
                reservationVM.setReservationBundle(bundle)

                if (mode == ReservationManagementMode.EDIT_MODE)
                    reservationVM.originalReservationBundle = bundle
            }

            // change selected date to the one of the selected slot, if there
            reservationVM.reservationBundle.value!!.getString("start_slot")?.let {
                val startSlot = LocalDateTime.parse(it)
                playgroundsVM.setSelectedDate(startSlot.toLocalDate())
            }
        }
    }

    // restrict the sport to show (just the one of the reservation to be edited, if any)
    sportIdToShow = reservationVM.originalReservationBundle?.getInt("sport_id")

    if (sportIdToShow == null || sportIdToShow == 0) {
        sportIdToShow = reservationVM.reservationBundle.value?.getInt("sport_id")

        if (sportIdToShow == 0) sportIdToShow = null
    }
}


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

            if (reservationVM.reservationManagementModeWrapper.mode == null) {
                // Playgrounds availabilities view -> show plus button
                addReservationButton!!.isVisible = true
            }
            else {
                // add/edit mode -> show add slot button
                addReservationSlotButton!!.isVisible = true

                if (!reservationVM.isStartSlotSet()) {
                    addReservationSlotButton?.icon = AppCompatResources.getDrawable(requireContext(),
                        R.drawable.baseline_more_time_24_blurred)
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
            R.id.add_reservation_button -> {
                navigateToAddReservation()
                true
            }
            R.id.add_reservation_slot_button -> {
                // navigate to next step (add/edit equipments) only if at least the start slot is selected
                if(reservationVM.isStartSlotSet())
                    navigateToManageEquipments()
                true
            }
            else -> false
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

internal fun PlaygroundAvailabilitiesFragment.initFloatingButton() {
    floatingButton = requireView().findViewById(R.id.floatingButton)
    floatingButton.setOnClickListener {
        if(reservationVM.isStartSlotSet())
            navigateToManageEquipments()
    }
    if(reservationVM.isStartSlotSet()){
        floatingButton.visibility = View.VISIBLE
    } else {
        floatingButton.visibility = View.GONE
    }

}

private fun PlaygroundAvailabilitiesFragment.navigateToAddReservation() {
    // go to add/edit mode
    findNavController().navigate(R.id.action_playgroundAvailabilitiesFragment_self)
}
private fun PlaygroundAvailabilitiesFragment.navigateToManageEquipments() {
    val selectedReservationInfo = reservationVM.reservationBundle.value

    if (selectedReservationInfo == null) {
        showToasty("error", requireContext(),
            "Error: cannot go to manage equipments without having a reservation",
            Toasty.LENGTH_LONG)
        return
    }

    val bundleStartSlot = selectedReservationInfo.getString("start_slot")
    val bundleEndSlot = selectedReservationInfo.getString("end_slot")
    val bundleSlotDurationMins = selectedReservationInfo.getInt("slot_duration_mins").toLong()
    val bundlePlaygroundId = selectedReservationInfo.getInt("playground_id")
    val bundlePlaygroundName = selectedReservationInfo.getString("playground_name")
    val bundleSportId = selectedReservationInfo.getInt("sport_id")
    val bundleSportEmoji = selectedReservationInfo.getString("sport_emoji")
    val bundleSportName = selectedReservationInfo.getString("sport_name")
    val bundleSportCenterId = selectedReservationInfo.getInt("sport_center_id")
    val bundleSportCenterName = selectedReservationInfo.getString("sport_center_name")
    val bundleSportCenterAddress = selectedReservationInfo.getString("sport_center_address")
    val bundlePlaygroundPricePerHour = selectedReservationInfo.getFloat("playground_price_per_hour")

    val errors = mutableListOf<String>()

    if (bundleStartSlot == null)
        errors.add("a start slot")

    if (bundleSlotDurationMins == 0L)
        errors.add("the slot duration mins")

    if (bundlePlaygroundId == 0)
        errors.add("a playground (id)")

    if(bundlePlaygroundName == null)
        errors.add("a playground (name)")

    if (bundleSportId == 0)
        errors.add("a sport (id)")

    if (bundleSportEmoji == null)
        errors.add("a sport (emoji)")

    if (bundleSportName == null)
        errors.add("a sport (name)")

    if(bundleSportCenterId == 0)
        errors.add("a sport center (id)")

    if(bundleSportCenterName == null)
        errors.add("a sport center (name)")

    if(bundleSportCenterAddress == null)
        errors.add("a sport center (address)")

    if(bundlePlaygroundPricePerHour == 0f)
        errors.add("a playground price per hour")

    if (errors.isNotEmpty()) {
        showToasty("error", requireContext(), errors.joinToString(
                prefix = "Error: cannot go to manage equipments without having selected ",
                separator = ", "
            ),
            Toasty.LENGTH_LONG)
        return
    }

    val params = bundleOf(
        "reservation" to selectedReservationInfo.also {
            if (bundleEndSlot == null) {
                // just one slot has been selected -> put manually the end one (as the same as the start slot)
                it.putString("end_slot", bundleStartSlot)
            }
        }
    )

    // go to manage equipments
    findNavController().navigate(
        R.id.action_playgroundAvailabilitiesFragment_to_manageEquipmentsFragment, params)
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
        R.layout.simple_spinner_item,
        playgroundsVM.sports.value ?: mutableListOf()
    ).also {
        it.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
    }

    // initialize sport Spinner
    selectedSportSpinner = requireView().findViewById(R.id.selected_sport_spinner)

    // if in edit mode, restrict it to contain one only sport
    if(reservationVM.isStartSlotSet()) {
        // set it as unclickable (and remove selection arrow)
        selectedSportSpinner.isEnabled = false
        selectedSportSpinner.isClickable = false
        selectedSportSpinner.setBackgroundResource(R.drawable.spinner_one_entry_only_bg)
    }

    // mount the adapter in the selected sport spinner
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

    playgroundsVM.currentMonth.observe(viewLifecycleOwner) { newMonth ->
        // change month label
        monthLabel.text = capitalize(
            newMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)))

        // retrieve new playground availabilities for the current month and the current sport
        playgroundsVM.updatePlaygroundAvailabilitiesForCurrentMonthAndSport()
    }

    /* dates view model observers */
    val selectedDateLabel = requireView().findViewById<TextView>(R.id.selected_date_label)
    val selectedDateLabelBox = requireView().findViewById<View>(R.id.selected_date_label_box)

    playgroundsVM.selectedDate.observe(viewLifecycleOwner) {
        // update calendar selected date
        calendarView.notifyDateChanged(it)

        // update selected date label
        selectedDateLabel.text = when(it) {
            LocalDate.now() -> "Today"
            LocalDate.now().plusDays(1) -> "Tomorrow"
            LocalDate.now().minusDays(1) -> "Yesterday"
            else -> it.format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM y", Locale.ENGLISH))
        }

        if (it < LocalDate.now()) {
            // set date label as grey
            selectedDateLabelBox.setBackgroundResource(R.color.time_slot_unavailable_background_color)
        }
        else {
            // set date label as orange
            selectedDateLabelBox.setBackgroundResource(R.drawable.time_slot_bg)
        }

        // update recycler view data
        playgroundAvailabilitiesAdapter.selectedDate = it

        // update time slots
        playgroundAvailabilitiesAdapter.smartUpdatePlaygroundAvailabilities(
            playgroundsVM.getAvailablePlaygroundsOnSelectedDate()
        )
    }

    playgroundsVM.previousSelectedDate.observe(viewLifecycleOwner) {
        calendarView.notifyDateChanged(it)
    }
}

internal fun PlaygroundAvailabilitiesFragment.initAvailablePlaygroundsObserver() {
    /* playground availabilities observer to change dates' colors */
    playgroundsVM.availablePlaygroundsPerSlot.observe(viewLifecycleOwner) {
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

        if(it.isNotEmpty()) {
            playgroundsVM.setAvailablePlaygroundsLoaded(true)
        }
        else {
            playgroundsVM.setAvailablePlaygroundsLoaded(false)
        }
    }
}

internal fun PlaygroundAvailabilitiesFragment.initSelectedSportObservers() {
    // sports observer
    playgroundsVM.sports.observe(viewLifecycleOwner) {
        // add new sports list
        try {
            selectedSportSpinnerAdapter.clear()
            selectedSportSpinnerAdapter.addAll(it)
        }
        catch (_: UnsupportedOperationException) { }
    }

    // selected sport observer
    playgroundsVM.selectedSport.observe(viewLifecycleOwner) {
        // empty current playground availabilities
        playgroundsVM.clearAvailablePlaygroundsPerSlot()

        // retrieve new playground availabilities for the current month and the current sport
        playgroundsVM.updatePlaygroundAvailabilitiesForCurrentMonthAndSport()
    }
}

/* Playground availabilities recycler view */
internal fun PlaygroundAvailabilitiesFragment.setupAvailablePlaygroundsRecyclerView() {
    playgroundAvailabilitiesRecyclerView = requireView().findViewById(R.id.playground_availabilities_rv)

    // init adapter
    playgroundAvailabilitiesAdapter = PlaygroundAvailabilitiesAdapter(
        playgroundsVM.getAvailablePlaygroundsOnSelectedDate(),
        playgroundsVM.selectedDate.value ?: playgroundsVM.defaultDate,
        playgroundsVM.slotDuration,
        reservationVM.reservationManagementModeWrapper,
        reservationVM.originalReservationBundle,
        reservationVM.reservationBundle.value,
        reservationVM::setReservationBundle,
        this::switchToAddMode
    ) { // callback to navigate to Playground details
        playgroundId, selectedSlot ->

        val params = bundleOf(
            "id_playground" to playgroundId,
            "selected_slot" to selectedSlot.toString()
        )
        // navigate to playground details view
        findNavController().navigate(
            R.id.action_playgroundAvailabilitiesFragment_to_PlaygroundDetailsFragment, params)
    }

    // init recycler view
    playgroundAvailabilitiesRecyclerView!!.apply {
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
    bottomBar.menu.findItem(R.id.slots).isChecked = true
}

/* add/edit mode */
internal fun PlaygroundAvailabilitiesFragment.setupAddOrEditModeView() {
    /* app bar */
    val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

    actionBar?.let {
        // show back arrow and the right title
        it.setDisplayHomeAsUpEnabled(true)
        it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        it.title = reservationVM.reservationManagementModeWrapper.mode!!.appBarTitle
    }

    /* menu */

    // show only add slot button
    addReservationButton?.isVisible = false
    addReservationSlotButton?.isVisible = true

    if(reservationVM.isStartSlotSet()){
        floatingButton.visibility = View.VISIBLE
    }

    /* month and date colors */

    // change current month color
    val selectedMonthBar = requireView().findViewById<View>(R.id.app_bar_layout)
    selectedMonthBar.setBackgroundResource(reservationVM.reservationManagementModeWrapper.mode!!.variantColorId)

    // change color to the selected date bar
    val selectedDateLabelBox = requireView().findViewById<View>(R.id.selected_date_label_box)
    selectedDateLabelBox.setBackgroundResource(reservationVM.reservationManagementModeWrapper.mode!!.variantColorId)

    // hide bottom bar
    val bottomBar = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)
    bottomBar.visibility = View.GONE
}

private fun PlaygroundAvailabilitiesFragment.switchToAddMode() {
    // change mode to ADD_MODE
    reservationVM.reservationManagementModeWrapper.mode = ReservationManagementMode.ADD_MODE

    // update recycler view adapter
    playgroundAvailabilitiesAdapter.reservationManagementModeWrapper.mode =
        reservationVM.reservationManagementModeWrapper.mode
    playgroundAvailabilitiesAdapter.reservationBundle = reservationVM.reservationBundle.value

    playgroundAvailabilitiesAdapter.smartUpdatePlaygroundAvailabilities(
        playgroundsVM.getAvailablePlaygroundsOnSelectedDate(),
        recreateAll = true
    )

    // update view
    this.setupAddOrEditModeView()
}

/* utils */

/** Compute availability percentage as the percentage of slots with at least one available playground */
private fun PlaygroundAvailabilitiesFragment.getAvailabilityPercentageOf(date: LocalDate): Float {
    val availablePlaygrounds = playgroundsVM.getAvailablePlaygroundsOn(date).mapValues {
        (_, playgrounds) -> playgrounds.filter {
            it.available
        }
    }

    val slotsWithAtLeastOneAvailability = availablePlaygrounds.count { it.value.isNotEmpty() }
    val maxNumOfSlotsPerDay = 25

    return min(1.0f, slotsWithAtLeastOneAvailability.toFloat() / maxNumOfSlotsPerDay.toFloat())
}

private fun capitalize(str: String): String {
    return str.substring(0,1).uppercase() + str.substring(1).lowercase()
}
