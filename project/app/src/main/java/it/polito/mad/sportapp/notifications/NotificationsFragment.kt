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
import it.polito.mad.sportapp.entities.room.RoomNotification
import it.polito.mad.sportapp.notifications.recyclerView.NotificationsAdapter

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    // view model
    internal lateinit var vm: SportAppViewModel

    internal var notifications = mutableListOf<RoomNotification>()

    // notifications recycler view
    internal lateinit var notificationsRecyclerView: RecyclerView
    internal val notificationsAdapter = NotificationsAdapter()

    // action bar
    internal var actionBar: ActionBar? = null

    // progress bar
    internal lateinit var progressBar: View

    // navigation controller
    internal lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize view model
        vm = ViewModelProvider(requireActivity())[SportAppViewModel::class.java]

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        // get progress bar
        progressBar = view.findViewById(R.id.progress_bar_notifications)

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