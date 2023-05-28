package it.polito.mad.sportapp.invitation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.invitation.users_recycler_view.UserAdapter

@AndroidEntryPoint
class InvitationFragment : Fragment(R.layout.fragment_invitation) {

    internal val viewModel by viewModels<InvitationViewModel>()
    internal var reservationId: String = ""
    internal var reservationSportId: String = ""

    internal lateinit var levelSpinner: Spinner
    internal lateinit var usernameSearch: EditText

    internal lateinit var usersRecyclerView: RecyclerView
    internal val userAdapter = UserAdapter(inviteButtonListener)

    // action bar
    internal var actionBar: ActionBar? = null

    private lateinit var bottomNavigationBar: View
    internal lateinit var navController: NavController

    private lateinit var fireListener: FireListener

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
        reservationId = arguments?.getString("id_reservation") ?: ""
        reservationSportId = arguments?.getString("id_sport") ?: ""

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
        fireListener = viewModel.getUsersFromDb(reservationId, reservationSportId)
    }

    override fun onDestroy() {
        super.onDestroy()
        fireListener.unregister()
    }
}