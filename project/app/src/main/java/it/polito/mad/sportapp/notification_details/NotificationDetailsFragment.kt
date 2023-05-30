package it.polito.mad.sportapp.notification_details

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.NotificationStatus

@AndroidEntryPoint
class NotificationDetailsFragment : Fragment(R.layout.fragment_notification_details) {

    // action bar
    internal var actionBar: ActionBar? = null

    // progress bar
    internal lateinit var progressBar: View

    // navigation controller
    internal lateinit var navController: NavController

    private var reservationId: String? = null
    internal lateinit var notificationStatus: NotificationStatus
    private lateinit var notificationTimestamp: String

    // dialogs
    internal lateinit var acceptInvitationDialog: AlertDialog
    internal lateinit var declineInvitationDialog: AlertDialog
    internal lateinit var rejectInvitationDialog: AlertDialog

    // notification details view model
    internal val vm by viewModels<NotificationDetailsViewModel>()

    // user reservations fire listener
    private lateinit var userReservationFireListener: FireListener

    // notification details main views
    internal lateinit var notificationDetailsScrollView: ScrollView
    internal lateinit var notificationDetailsCanceledMessage: ConstraintLayout
    internal lateinit var notificationDetailsRejectedMessage: TextView
    internal lateinit var notificationDetailsJoinQuestion: TextView

    // notification details text views
    internal lateinit var reservationOwner: TextView
    internal lateinit var playgroundName: TextView
    internal lateinit var sportCenterName: TextView
    internal lateinit var sportEmoji: TextView
    internal lateinit var sportName: TextView
    internal lateinit var sportCenterAddress: TextView
    internal lateinit var reservationDate: TextView
    internal lateinit var reservationStartTime: TextView
    internal lateinit var reservationEndTime: TextView
    internal lateinit var reservationPricePerHour: TextView

    // notification details buttons
    internal lateinit var acceptInvitationButton: Button
    internal lateinit var declineInvitationButton: Button
    internal lateinit var rejectInvitationButton: Button
    internal lateinit var reservationCanceledButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // get progress bar
        progressBar = view.findViewById(R.id.progress_bar_notifications_details)

        // initialize navigation controller
        navController = findNavController()

        // retrieve reservation id
        reservationId = arguments?.getString("id_reservation")

        // retrieve notification status
        notificationStatus =
            NotificationStatus.from(arguments?.getString("status") ?: "CANCELED")

        // retrieve notification timestamp
        notificationTimestamp = arguments?.getString("timestamp") ?: ""

        // retrieve reservation from db or set notification status to canceled
        if (reservationId != null) {
            userReservationFireListener = vm.getReservationFromDb(reservationId!!)
        } else {
            notificationStatus = NotificationStatus.CANCELED
        }

        // initialize views

        notificationDetailsScrollView =
            requireView().findViewById(R.id.notification_details_scroll_view)
        notificationDetailsCanceledMessage =
            requireView().findViewById(R.id.notification_canceled_layout)
        notificationDetailsRejectedMessage =
            requireView().findViewById(R.id.notification_details_rejected_message)
        notificationDetailsJoinQuestion =
            requireView().findViewById(R.id.notification_details_join_question)

        reservationOwner =
            requireView().findViewById(R.id.notification_details_reservation_owner_username)
        playgroundName = requireView().findViewById(R.id.notification_details_playground_name)
        sportCenterName = requireView().findViewById(R.id.notification_details_sport_center_name)
        sportEmoji = requireView().findViewById(R.id.notification_details_sport_emoji)
        sportName = requireView().findViewById(R.id.notification_details_sport_name)
        sportCenterAddress =
            requireView().findViewById(R.id.notification_details_playground_address)
        reservationDate = requireView().findViewById(R.id.notification_details_date)
        reservationStartTime = requireView().findViewById(R.id.notification_details_start_time)
        reservationEndTime = requireView().findViewById(R.id.notification_details_end_time)
        reservationPricePerHour =
            requireView().findViewById(R.id.notification_details_playground_price_per_hour)

        // setup reservation observers
        setupObservers()

        // initialize menu
        menuInit(true)

        // initialize dialogs
        initAcceptInvitationDialog()
        initDeclineInvitationDialog()
        initRejectInvitationDialog()

        // initialize buttons
        initButtons()

        // setup the right views
        manageNotificationState()

        // setup bottom bar
        setupBottomBar()
    }

    override fun onDestroy() {
        super.onDestroy()

        // remove user reservation listener
        userReservationFireListener.unregister()
    }

}