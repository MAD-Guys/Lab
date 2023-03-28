package it.polito.mad.lab2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
//import com.google.android.material.chip.Chip

class ShowProfileActivity : AppCompatActivity() {
    private lateinit var addFriendButton: Button
    private lateinit var messageButton: Button
    // private lateinit var chip: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        // manage sports chips
        // chip = findViewById(R.id.chipSport1)
        // chip.visibility = Chip.GONE

        // retrieve buttons and set callbacks
        addFriendButton = findViewById(R.id.button_add_friend)
        messageButton = findViewById(R.id.button_message)

        addFriendButton.setOnClickListener() {
            val toast = Toast.makeText(this, "Add friend button clicked!!!", Toast.LENGTH_SHORT)
            toast.show()
        }

        messageButton.setOnClickListener() {
            val toast = Toast.makeText(this, "Message button clicked!!!", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.profile_menu, menu)
        // change app bar's title
        supportActionBar?.title = "Profile"
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        // detect which item has been selected and perform corresponding action
        R.id.edit_button -> handleEditButton()
        else -> super.onOptionsItemSelected(item)
    }

    private fun handleEditButton(): Boolean {
        // Launching the EditProfileActivity
        val editProfileIntent = Intent(this, EditProfileActivity::class.java)
        startActivity(editProfileIntent)

        val toast = Toast.makeText(this, "Now you are in EDIT mode!", Toast.LENGTH_SHORT)
        toast.show()
        return true
    }

}