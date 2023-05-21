package it.polito.mad.sportapp.notifications

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.SportAppViewModel
import it.polito.mad.sportapp.notifications.recyclerView.NotificationsAdapter
import java.time.LocalDate
import java.time.LocalTime

data class Notification(
    val notificationId: Int,
    val username: String,
    val sportName: String,
    val sportCenterName: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
)

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    // view model
    internal lateinit var vm: SportAppViewModel

    internal var notifications = mutableListOf<Notification>()

    // notifications recycler view
    internal lateinit var notificationsRecyclerView: RecyclerView
    internal val notificationsAdapter = NotificationsAdapter()

    // action bar
    internal var actionBar: ActionBar? = null

    // navigation controller
    internal lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize view model
        vm = ViewModelProvider(requireActivity())[SportAppViewModel::class.java]

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // initialize navigation controller
        navController = findNavController()

        // initialize menu
        menuInit()

        // setup bottom bar
        setupBottomBar()

        // initialize recycler view
        recyclerViewInit()
    }
}