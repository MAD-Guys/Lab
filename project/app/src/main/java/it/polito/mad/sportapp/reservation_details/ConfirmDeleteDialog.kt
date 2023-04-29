package it.polito.mad.sportapp.reservation_details

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.show_reservations.ShowReservationsActivity

class ConfirmDeleteDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage("This reservation will be deleted. Are you sure?")
            .setPositiveButton("YES, DELETE RESERVATION") { d, _ ->
                val vm = activity?.viewModels<ReservationDetailsViewModel> {
                    ViewModelProvider.AndroidViewModelFactory.getInstance(this.requireActivity().application)
                }
                vm?.value!!.deleteReservation()
                val intent = Intent(requireContext(), ShowReservationsActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("NO, GO BACK") { d,_ -> d.cancel() }
            .create()

    companion object {
        const val TAG = "PurchaseConfirmationDialog"
    }
}