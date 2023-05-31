package it.polito.mad.sportapp.invitation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.application_utilities.showToasty
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.invitation.users_recycler_view.UserAdapter

@AndroidEntryPoint
class InvitationFragment : Fragment(R.layout.fragment_invitation) {

    internal val viewModel by viewModels<InvitationViewModel>()

    internal var reservationId: String = ""
    private var reservationSportId: String = ""
    private var reservationSportName: String = ""

    private lateinit var sportLabel: TextView

    internal lateinit var levelSpinner: Spinner
    internal lateinit var usernameSearch: EditText

    internal lateinit var usersRecyclerView: RecyclerView
    internal lateinit var userAdapter: UserAdapter

    // action bar
    internal var actionBar: ActionBar? = null

    private lateinit var bottomNavigationBar: View
    internal lateinit var navController: NavController

    private lateinit var fireListener: FireListener

    @SuppressLint("SetTextI18n")
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

        sportLabel = view.findViewById(R.id.invitation_box_sport_label)

        // Retrieve event id
        reservationId = arguments?.getString("id_reservation") ?: ""
        reservationSportId = arguments?.getString("id_sport") ?: ""
        reservationSportName = arguments?.getString("sport_name") ?: ""

        val reservationSportEmoji = arguments?.getString("sport_emoji") ?: ""
        sportLabel.text = "$reservationSportName  $reservationSportEmoji"

        // Initialize fireListener
        fireListener = viewModel.getUsersFromDb(reservationId, reservationSportId, reservationSportName)

        // Retrieve views
        usernameSearch = requireView().findViewById(R.id.search_username)
        usersRecyclerView = requireView().findViewById(R.id.users_container)

        // Init views
        initLevelSpinner()
        usernameSearch.addTextChangedListener(textListenerInit())

        // init users recycler view adapter
        userAdapter = UserAdapter(inviteButtonListener, reservationSportId)

        //set observers
        viewModel.users.observe(viewLifecycleOwner) {
            if (viewModel.users.value != null) {
                initUserList()
            }
        }

        // clear errors
        viewModel.clearErrors()

        viewModel.getError.observe(viewLifecycleOwner) {
            if(it != null){
                // show error toast
                showToasty("error", requireContext(), it.message())

                // go back
                navController.popBackStack()
            }
        }

        viewModel.invitationError.observe(viewLifecycleOwner) {
            if(it != null){
                // show error toast
                showToasty("error", requireContext(), it.message())
            }
        }

        viewModel.invitationSuccess.observe(viewLifecycleOwner) {
            if(it != null){ // it is null or the username of the invited user
                // show success toast
                showToasty("success", requireContext(), "Invitation successfully sent to $it!")
                viewModel.clearInvitationSuccess()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fireListener.unregister()
    }
}