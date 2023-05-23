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
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.playground_details.PlaygroundDetailsFragment
import it.polito.mad.sportapp.playground_details.handleAddReservationButton

class InvitationFragment : Fragment() {

    internal val viewModel by viewModels<InvitationViewModel>()
    internal var reservationId: Int = -1
    internal var reservationSportId: Int = -1

    private lateinit var levelSpinner: Spinner
    internal lateinit var usernameSearch: EditText

    private lateinit var usersRecyclerView: RecyclerView

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
        levelSpinner = requireView().findViewById(R.id.level_spinner)
        usernameSearch = requireView().findViewById(R.id.search_username)
        usersRecyclerView = requireView().findViewById(R.id.users_container)

        // Init views
        //TODO init level spinner

        usernameSearch.addTextChangedListener(textListenerInit())
    }

    //set observers
    /* TODO

    viewModel.users.observe(viewLifecycleOwner) {
        if (viewModel.users.value != null) {
            initUserList()
        }
    }

    */
}

private fun InvitationFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.reservations_details_menu, menu)

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
                it.title = "Send invitations"
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return false
        }

    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

private fun InvitationFragment.textListenerInit(): TextWatcher {

    return object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            viewModel.searchUsers(usernameSearch.text.toString())
        }

        override fun afterTextChanged(p0: Editable?) {
            viewModel.searchUsers(usernameSearch.text.toString())
        }

    }

}