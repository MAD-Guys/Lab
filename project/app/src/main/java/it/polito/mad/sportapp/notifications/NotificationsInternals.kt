package it.polito.mad.sportapp.notifications

import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.Notification
import it.polito.mad.sportapp.entities.NotificationStatus

// manage menu item selection
internal fun NotificationsFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.notifications_menu, menu)

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(false)
                it.title = "Notifications"
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // handle the menu selection
            return false
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

/* notifications recycler view */
@SuppressLint("NotifyDataSetChanged")
internal fun NotificationsFragment.recyclerViewInit() {

    // initialize notifications RecyclerView from layout
    notificationsRecyclerView = requireView().findViewById(R.id.notifications_recycler_view)

    notificationsRecyclerView.apply {
        layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = notificationsAdapter
    }

    // setup notifications observer
    vm.notifications.observe(viewLifecycleOwner) { notificationList ->

        if (notificationList.isEmpty()) {
            // hide progress bar and hide notifications
            progressBar.visibility = View.GONE
            notificationsRecyclerView.visibility = View.GONE
        } else {

            // hide progress bar and show notifications
            progressBar.visibility = View.GONE
            notificationsRecyclerView.visibility = View.VISIBLE

            // add notifications to the notifications adapter
            notificationsAdapter.notifications.clear()
            notificationsAdapter.notifications.addAll(notificationList
                .filter {
                    it.status == NotificationStatus.PENDING || it.status == NotificationStatus.ACCEPTED
                }
                .sortedWith(compareByDescending<Notification> { it.publicationDate }.thenByDescending { it.publicationTime })
            )
        }

        notificationsAdapter.notifyDataSetChanged()
    }
}

/* bottom bar */
internal fun NotificationsFragment.setupBottomBar() {
    // show bottom bar
    val bottomBar =
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)

    bottomBar.visibility = View.VISIBLE

    // set the right selected button
    bottomBar.menu.findItem(R.id.notifications).isChecked = true
}