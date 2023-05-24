package it.polito.mad.sportapp.notification_details

import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.NotificationStatus
import it.polito.mad.sportapp.application_utilities.showToasty
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// manage menu item selection
internal fun NotificationDetailsFragment.menuInit(displayHomeEnabled: Boolean) {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.notification_details_menu, menu)

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(displayHomeEnabled)
                it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
                it.title = "Notification Details"
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // handle the menu selection
            return false
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

internal fun NotificationDetailsFragment.setupObservers() {
    vm.reservation.observe(viewLifecycleOwner) {
        if (it != null) {

            progressBar.visibility = View.GONE
            notificationDetailsScrollView.visibility = View.VISIBLE

            val usernameString = "@" + it.username + " invited you to this event:"
            reservationOwner.text = usernameString

            playgroundName.text = it.playgroundName
            sportCenterName.text = it.sportCenterName
            sportEmoji.text = it.sportEmoji
            sportName.text = it.sportName
            sportCenterAddress.text = it.address

            reservationDate.text = when (it.startLocalDateTime.toLocalDate()) {
                LocalDate.now() -> "Today"
                LocalDate.now().plusDays(1) -> "Tomorrow"
                LocalDate.now().minusDays(1) -> "Yesterday"
                else -> it.startLocalDateTime.format(
                    DateTimeFormatter.ofPattern("EEEE, d MMMM y", Locale.ENGLISH)
                )
            }

            reservationStartTime.text = it.startTime.toString()
            reservationEndTime.text = it.endTime.toString()
            reservationPricePerHour.text = String.format("%.2f", it.playgroundPricePerHour)
        } else {
            progressBar.visibility = View.VISIBLE
            notificationDetailsScrollView.visibility = View.GONE
        }
    }
}

/* Accept invitation dialog */
internal fun NotificationDetailsFragment.initAcceptInvitationDialog() {
    acceptInvitationDialog = AlertDialog.Builder(requireContext())
        .setMessage("Do you want to accept this invitation?")
        .setPositiveButton("YES") { _, _ ->
            showToasty("success", requireContext(), "Invitation correctly accepted!")

            //TODO: update firestore db

            // navigate back
            navController.popBackStack()
        }
        .setNegativeButton("NO") { d, _ -> d.cancel() }
        .create()
}

/* Decline reservation dialog */
internal fun NotificationDetailsFragment.initDeclineInvitationDialog() {
    declineInvitationDialog = AlertDialog.Builder(requireContext())
        .setMessage("Do you want to decline this invitation?")
        .setPositiveButton("YES") { _, _ ->

            showToasty("success", requireContext(), "Invitation correctly declined!")

            //TODO: update firestore db

            // navigate back
            navController.popBackStack()
        }
        .setNegativeButton("NO") { d, _ -> d.cancel() }
        .create()
}

/* Reject reservation dialog */
internal fun NotificationDetailsFragment.initRejectInvitationDialog() {
    rejectInvitationDialog = AlertDialog.Builder(requireContext())
        .setMessage("Do you want to reject the previously accepted invitation?")
        .setPositiveButton("YES") { _, _ ->

            showToasty("success", requireContext(), "Invitation correctly rejected!")

            //TODO: update firestore db

            // navigate back
            navController.popBackStack()
        }
        .setNegativeButton("NO") { d, _ -> d.cancel() }
        .create()
}

/* notification state */
@SuppressLint("SetTextI18n")
internal fun NotificationDetailsFragment.manageNotificationState() {

    when (notificationStatus) {
        NotificationStatus.ACCEPTED -> {
            notificationDetailsScrollView.visibility = View.VISIBLE
            notificationDetailsCanceledMessage.visibility = View.GONE
            notificationDetailsRejectedMessage.visibility = View.GONE
            notificationDetailsJoinQuestion.text = "Do you want to reject this invitation?"
            acceptInvitationButton.visibility = View.GONE
            declineInvitationButton.visibility = View.GONE
            rejectInvitationButton.visibility = View.VISIBLE
        }

        NotificationStatus.REJECTED -> {
            notificationDetailsScrollView.visibility = View.VISIBLE
            notificationDetailsCanceledMessage.visibility = View.GONE
            notificationDetailsRejectedMessage.visibility = View.VISIBLE
            notificationDetailsJoinQuestion.visibility = View.GONE
            acceptInvitationButton.visibility = View.GONE
            declineInvitationButton.visibility = View.GONE
            rejectInvitationButton.visibility = View.GONE
        }

        NotificationStatus.CANCELED -> {
            menuInit(false)
            notificationDetailsScrollView.visibility = View.GONE
            notificationDetailsCanceledMessage.visibility = View.VISIBLE
        }

        else -> {
            notificationDetailsScrollView.visibility = View.VISIBLE
            notificationDetailsCanceledMessage.visibility = View.GONE
            notificationDetailsRejectedMessage.visibility = View.GONE
            notificationDetailsJoinQuestion.visibility = View.VISIBLE
            acceptInvitationButton.visibility = View.VISIBLE
            declineInvitationButton.visibility = View.VISIBLE
            rejectInvitationButton.visibility = View.GONE
        }
    }
}

/* buttons */
internal fun NotificationDetailsFragment.initButtons() {

    // accept invitation button
    acceptInvitationButton = requireView().findViewById(R.id.notification_details_accept_button)
    acceptInvitationButton.setOnClickListener {
        acceptInvitationDialog.show()
    }

    // decline invitation button
    declineInvitationButton = requireView().findViewById(R.id.notification_details_decline_button)
    declineInvitationButton.setOnClickListener {
        declineInvitationDialog.show()
    }

    // reject invitation button
    rejectInvitationButton = requireView().findViewById(R.id.notification_details_reject_button)
    rejectInvitationButton.setOnClickListener {
        rejectInvitationDialog.show()
    }

    // reservation canceled button
    reservationCanceledButton = requireView().findViewById(R.id.reservation_canceled_button)
    reservationCanceledButton.setOnClickListener {
        navController.navigate(R.id.action_notificationDetailsFragment_to_showReservationsFragment)
    }

}

/* bottom bar */
internal fun NotificationDetailsFragment.setupBottomBar() {
    // hide bottom bar
    val bottomBar =
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)

    bottomBar.visibility = View.GONE
}