package it.polito.mad.sportapp.invitation

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.invitation.users_recycler_view.UserAdapter
import it.polito.mad.sportapp.playground_details.PlaygroundDetailsFragment
import it.polito.mad.sportapp.playground_details.handleAddReservationButton
@AndroidEntryPoint
class InvitationFragment : Fragment(R.layout.fragment_invitation) {

    internal val viewModel by viewModels<InvitationViewModel>()
    internal var reservationId: Int = -1
    internal var reservationSportId: Int = -1

    internal lateinit var levelSpinner: Spinner
    internal lateinit var usernameSearch: EditText

    internal lateinit var usersRecyclerView: RecyclerView
    internal val userAdapter = UserAdapter()

    // action bar
    internal var actionBar: ActionBar? = null

    private lateinit var bottomNavigationBar: View
    internal lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        bottomNavigationBar =
            (requireActivity() as AppCompatActivity).findViewById(R.id.bottom_navigation_bar)

        bottomNavigationBar.visibility = View.GONE

        // initialize menu
        menuInit()

        // initialize navigation controller
        navController = Navigation.findNavController(view)

        // Retrieve event id
        reservationId = arguments?.getInt("id_reservation") ?: -1
        reservationSportId = arguments?.getInt("id_sport") ?: -1

        // Retrieve views
        usernameSearch = requireView().findViewById(R.id.search_username)
        usersRecyclerView = requireView().findViewById(R.id.users_container)

        // Init views
        initLevelSpinner()
        usernameSearch.addTextChangedListener(textListenerInit())

        //set observers
        viewModel.users.observe(viewLifecycleOwner) {
            if (viewModel.users.value != null) {
                initUserList()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUsersFromDb(reservationSportId)
    }
}