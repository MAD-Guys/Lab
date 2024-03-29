package it.polito.mad.sportapp.show_reservations.events_list_view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.show_reservations.ShowReservationsViewModel
import it.polito.mad.sportapp.show_reservations.events_list_view.events_list_recycler_view.EventsListAdapter
import java.time.LocalDate

@AndroidEntryPoint
class EventsListFragment : Fragment(R.layout.fragment_events_list) {

    private val eventsListAdapter = EventsListAdapter()

    private lateinit var eventsListView: RecyclerView

    private var actionBar: ActionBar? = null
    private lateinit var bottomNavigationBar: View
    private lateinit var navController: NavController

    // events list view model
    private lateinit var vm: ShowReservationsViewModel

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // retrieve view model from activity
        vm = ViewModelProvider(requireActivity())[ShowReservationsViewModel::class.java]

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // get bottom navigation bar
        bottomNavigationBar =
            (requireActivity() as AppCompatActivity).findViewById(R.id.bottom_navigation_bar)

        // hide bottom navigation bar
        bottomNavigationBar.visibility = View.GONE

        // initialize navigation controller
        navController = findNavController()

        // menu initialization
        menuInit()

        val currentDate = LocalDate.now()

        // initialize RecyclerView from layout
        eventsListView = requireView().findViewById(R.id.events_list_recycler_view)

        eventsListView.apply {
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = eventsListAdapter
        }

        vm.userEvents.observe(viewLifecycleOwner) {

            val noAvailableReservationsBox = view.findViewById<RelativeLayout>(R.id.no_available_reservations_box)

            if (it == null || it.isEmpty()) {
                noAvailableReservationsBox.visibility = View.VISIBLE
                eventsListView.visibility = View.GONE
            } else {

                noAvailableReservationsBox.visibility = View.GONE
                eventsListView.visibility = View.VISIBLE

                eventsListAdapter.events.clear()

                // add events to the adapter
                eventsListAdapter.events.addAll(it.values.flatten())
                eventsListAdapter.notifyDataSetChanged()
                scrollToCurrentDate(currentDate)
            }
        }
    }

    // manage menu item selection
    private fun menuInit() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.show_reservations_menu, menu)

                actionBar?.let {
                    it.setDisplayHomeAsUpEnabled(false)
                    it.title = "Reservations"
                }

                // change visibility of the show reservations menu item
                menu.findItem(R.id.show_reservations_button).isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // handle the menu selection
                return when (menuItem.itemId) {
                    R.id.show_reservations_button -> {
                        navController.popBackStack()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    // scroll to the current date event
    private fun scrollToCurrentDate(date: LocalDate) {
        val indexToScroll = if (vm.userEvents.value?.containsKey(date) == true) {
            eventsListAdapter.events.indexOf(vm.userEvents.value?.get(date)?.first())
        } else {
            getNextEvent(date)
        }
        // scroll to current date event
        eventsListView.layoutManager?.scrollToPosition(indexToScroll)
    }

    // get index from the nearest current date event if the event with current day is not available
    private fun getNextEvent(currentDate: LocalDate): Int {
        val nextEvent = vm.userEvents.value?.keys?.find { it.isAfter(currentDate) }

        return if (nextEvent != null) {
            eventsListAdapter.events.indexOf(vm.userEvents.value?.get(nextEvent)?.first())
        } else {
            eventsListAdapter.events.size - 1
        }
    }

}