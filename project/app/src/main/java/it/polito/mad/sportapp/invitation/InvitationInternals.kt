package it.polito.mad.sportapp.invitation

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.application_utilities.showToasty

internal fun InvitationFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
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

internal fun InvitationFragment.textListenerInit(): TextWatcher {

    return object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            viewModel.searchUsersByUsername(usernameSearch.text.toString())
            showToasty("info", requireContext(), "Search ${usernameSearch.text}")
        }

        override fun afterTextChanged(p0: Editable?) {
            viewModel.searchUsersByUsername(usernameSearch.text.toString())
            showToasty("info", requireContext(), "Search ${usernameSearch.text}")
        }

    }
}

internal fun InvitationFragment.initLevelSpinner() {
    // create the spinner adapter
    val spinnerAdapter = ArrayAdapter(
        requireContext(),
        R.layout.level_spinner_item,
        listOf("All", "Beginner", "Intermediate", "Expert", "Pro")
    ).also {
        it.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
    }

    // initialize Spinner
    levelSpinner = requireView().findViewById(R.id.level_spinner)

    // mount the adapter in the selected sport spinner
    levelSpinner.adapter = spinnerAdapter

    // specify the spinner callback to call at each user selection
    levelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            // retrieve selected option
            val justSelectedLevel = spinnerAdapter.getItem(position)
            // update selected level
            viewModel.searchUsersByLevel(justSelectedLevel!!)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            viewModel.searchUsersByLevel("All")
        }
    }
}

@SuppressLint("NotifyDataSetChanged")
internal fun InvitationFragment.initUserList() {
    usersRecyclerView.apply {
        layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = userAdapter
    }
    userAdapter.users.clear()
    viewModel.users.value?.let {
        userAdapter.users.addAll(it)
    }
    userAdapter.notifyDataSetChanged()
}

internal val InvitationFragment.inviteButtonListener: (Int, String) -> Unit
    get() = { userId: Int, username: String ->

        AlertDialog.Builder(requireContext())
            .setMessage("Do you want to send an invitation to @$username?")
            .setPositiveButton("YES") { _, _ ->
                viewModel.sendInvitation(userId, reservationId)
                showToasty("success", this.requireContext(), "Invitation sent to @$username")
            }
            .setNegativeButton("NO") { d, _ -> d.cancel() }
            .create()
            .show()
    }