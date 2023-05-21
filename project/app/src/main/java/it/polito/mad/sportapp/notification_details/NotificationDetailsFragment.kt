package it.polito.mad.sportapp.notification_details

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.showToasty
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@AndroidEntryPoint
class NotificationDetailsFragment : Fragment(R.layout.fragment_notification_details) {

    // action bar
    internal var actionBar: ActionBar? = null

    // navigation controller
    internal lateinit var navController: NavController

    private var reservationId: Int = -1
    internal var publicationDate: LocalDate? = null
    internal var publicationTime: LocalTime? = null

    // notification details view model
    internal val vm by activityViewModels<NotificationDetailsViewModel>()

    internal lateinit var notificationDetailsTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // initialize navigation controller
        navController = findNavController()

        // Retrieve reservation id, publication date and publication time
        reservationId = arguments?.getInt("id_reservation") ?: -1
        publicationDate =
            arguments?.getString("timestamp")?.let { LocalDateTime.parse(it).toLocalDate() }
        publicationTime =
            arguments?.getString("timestamp")?.let { LocalDateTime.parse(it).toLocalTime() }

        // retrieve reservation from db
        vm.getReservationFromDb(reservationId)

        // setup reservation observer
        vm.reservation.observe(viewLifecycleOwner) {
            if (it != null) {
                notificationDetailsTextView = view.findViewById(R.id.tv_notification_details)
                notificationDetailsTextView.text = it.toString()
            }
        }

        showToasty(
            "info",
            requireContext(),
            "Reservation id: $reservationId, publication date: $publicationDate, publication time: $publicationTime"
        )

        // initialize menu
        menuInit()

        // setup bottom bar
        setupBottomBar()
    }

}