package it.polito.mad.sportapp.playground_availabilities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.navigateTo
import it.polito.mad.sportapp.profile.ShowProfileActivity
import it.polito.mad.sportapp.show_reservations.ShowReservationsActivity

class PlaygroundAvailabilitiesActivity : AppCompatActivity() {
    // View Model
    internal val viewModel by viewModels<PlaygroundAvailabilitiesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playground_availabilities)

        // TODO
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.playground_availabilities_menu, menu)

        // change app bar's title
        supportActionBar?.title = "Playground availabilities"

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.show_reservations_button -> navigateTo(ShowReservationsActivity::class.java)
        R.id.show_profile_button -> navigateTo(ShowProfileActivity::class.java)
        else -> super.onOptionsItemSelected(item)
    }
}