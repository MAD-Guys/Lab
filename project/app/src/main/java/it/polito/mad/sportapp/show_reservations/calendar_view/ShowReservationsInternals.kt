package it.polito.mad.sportapp.show_reservations.calendar_view

import android.annotation.SuppressLint
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.application_utilities.hideProgressBar
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/* Internals functions related to the Show Reservations Activity */

// initialize month buttons
internal fun ShowReservationsFragment.monthButtonsInit() {

    // initialize previous / next month buttons and their listeners
    previousMonthButton = requireView().findViewById(R.id.previous_month_button)

    previousMonthButton.setOnClickListener {
        calendarView.findFirstVisibleMonth()?.let { month ->
            val newMonth = month.yearMonth.minusMonths(1)
            calendarView.smoothScrollToMonth(newMonth)
        }
    }

    nextMonthButton = requireView().findViewById(R.id.next_month_button)

    nextMonthButton.setOnClickListener {
        calendarView.findFirstVisibleMonth()?.let { month ->
            val newMonth = month.yearMonth.plusMonths(1)
            calendarView.smoothScrollToMonth(newMonth)
        }
    }
}

// event adapter for date
@SuppressLint("NotifyDataSetChanged")
fun ShowReservationsFragment.updateAdapterForDate(date: LocalDate?) {
    eventsAdapter.events.clear()
    eventsAdapter.events.addAll(vm.userEvents.value?.get(date).orEmpty())
    eventsAdapter.notifyDataSetChanged()
}

// initialize calendar information
internal fun ShowReservationsFragment.calendarInit() {
    val currentDate = LocalDate.now()

    // bind days to the calendar recycler view
    calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
        // call only when a new container is needed.
        override fun create(view: View) =
            DayViewContainer(view, vm, updater = ::updateAdapterForDate)

        // call every time we need to reuse a container.
        override fun bind(container: DayViewContainer, data: CalendarDay) {

            // day layouts and views
            val dayRelativeLayout = container.relativeLayout
            val dayTextView = container.textView
            val eventTag = container.eventTag

            // set visibility of day text view as visible
            dayTextView.visibility = View.VISIBLE

            // set visibility of event tag as gone
            eventTag.visibility = View.GONE

            container.day = data
            dayTextView.text = data.date.dayOfMonth.toString()

            if (data.position == DayPosition.MonthDate) {

                val events = vm.userEvents.value?.get(data.date)

                // set background color and text for month dates
                dayRelativeLayout.setBackgroundColor(
                    getColor(
                        requireContext(),
                        R.color.month_date_background
                    )
                )
                dayTextView.setTextColor(getColor(requireContext(), R.color.month_date_text_color))

                // mark current date
                if (data.date == currentDate) {
                    dayTextView.setTextColor(
                        getColor(
                            requireContext(),
                            R.color.current_date_text_color
                        )
                    )
                    dayRelativeLayout.setBackgroundResource(R.drawable.current_day_selected_bg)
                }

                // mark selected date
                if (data.date != currentDate && data.date == vm.selectedDate.value) {
                    dayTextView.setTextColor(
                        getColor(
                            requireContext(),
                            R.color.selected_date_text_color
                        )
                    )
                    dayRelativeLayout.setBackgroundResource(R.drawable.day_selected_bg)
                }

                // display events tags and events, if any
                if (events != null) {
                    eventTag.visibility = View.VISIBLE

                    if (data.date.isBefore(currentDate)) {
                        eventTag.setColorFilter(
                            getColor(
                                requireContext(),
                                R.color.past_event_tag_color
                            )
                        )
                    } else {
                        eventTag.setColorFilter(getColor(requireContext(), R.color.event_tag_color))
                    }

                    if (YearMonth.from(data.date) == vm.currentMonth.value && data.date == vm.selectedDate.value) {

                        // perform haptic feedback if the day clicked has at least one event
                        performHapticFeedback(container.view)
                        updateAdapterForDate(data.date)
                    }
                } else {

                    eventTag.visibility = View.GONE

                    if (YearMonth.from(data.date) == vm.currentMonth.value && data.date == vm.selectedDate.value) {
                        updateAdapterForDate(null)
                    }
                }

            }
            // set background color and text for out dates
            else {
                dayRelativeLayout.setBackgroundColor(
                    getColor(
                        requireContext(),
                        R.color.out_date_background_color
                    )
                )
                dayTextView.setTextColor(getColor(requireContext(), R.color.out_date_text_color))
            }

        }
    }

    // initialize user events live data variable
    vm.userEvents.observe(viewLifecycleOwner) {

        if (it != null) {
            hideProgressBar(progressBar, recyclerView)

            // show events for the selected date if any
            vm.selectedDate.value?.let { date ->
                if (date != currentDate) {
                    updateAdapterForDate(date)
                } else {
                    updateAdapterForDate(currentDate)
                }
            }

            calendarView.notifyMonthChanged(vm.currentMonth.value!!)
        }
    }

    // initialize current month live data variable
    vm.currentMonth.observe(viewLifecycleOwner) {

        // change month label
        val monthString = it.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
        monthLabel.text = capitalizeFirstLetter(monthString)

        //update calendar
        calendarView.notifyMonthChanged(it)
        calendarView.notifyMonthChanged(it.minusMonths(1))
        calendarView.notifyMonthChanged(it.plusMonths(1))
    }

    // initialize selected date live data variable
    vm.selectedDate.observe(viewLifecycleOwner) {
        // update selected date
        calendarView.notifyDateChanged(it)
    }

    // initialize previous selected date live data variable
    vm.previousSelectedDate.observe(viewLifecycleOwner) {
        // update previous selected date
        calendarView.notifyDateChanged(it)
    }

    // initialize days of week to monday
    val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)

    legendContainer.children
        .map { it as TextView }
        .forEachIndexed { index, textView ->
            val dayOfWeek = daysOfWeek[index]
            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            textView.text = title
        }

    // attach month scroll listener
    calendarView.monthScrollListener = { newMonth ->
        vm.setCurrentMonth(newMonth.yearMonth)
    }

    // finalize calendar view initialization
    vm.currentMonth.value?.let {
        val startMonth = it.minusMonths(100)
        val endMonth = it.plusMonths(100)

        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(it)
    }
}

// manage menu item selection
internal fun ShowReservationsFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.show_reservations_menu, menu)

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(false)
                it.title = "Reservations"
            }

            // change visibility of the show reservations menu items
            menu.findItem(R.id.events_list_button).isVisible = true
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // handle the menu selection
            return when (menuItem.itemId) {
                R.id.events_list_button -> {
                    navController.navigate(R.id.action_showReservationsFragment_to_eventsListFragment)
                    true
                }

                else -> false
            }
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

// bottom bar setup
internal fun ShowReservationsFragment.setupBottomBar() {
    // show bottom navigation bar
    val bottomNavigationBar: BottomNavigationView =
        requireActivity().findViewById(R.id.bottom_navigation_bar)
    bottomNavigationBar.visibility = View.VISIBLE

    // set the right button
    bottomNavigationBar.menu.findItem(R.id.reservations).isChecked = true
}

// setup on back pressed callback
internal fun ShowReservationsFragment.setupOnBackPressedCallback() {
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitDialog.show()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner, // LifecycleOwner
        callback
    )
}

/* Exit and logout dialog */
internal fun ShowReservationsFragment.exitDialogInit() {
    exitDialog = AlertDialog.Builder(requireContext())
        .setMessage("Do you want exit from the application?")
        .setPositiveButton("YES") { _, _ ->
            // finish the activity
            requireActivity().finish()
        }
        .setNegativeButton("NO") { d, _ -> d.cancel() }
        .create()
}

// perform haptic feedback if android version is lower than 13
@Suppress("DEPRECATION")
internal fun performHapticFeedback(view: View) {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        view.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}

// capitalize the first letter of the string
internal fun capitalizeFirstLetter(string: String): String {
    return string.substring(0, 1).uppercase() + string.substring(1).lowercase()
}