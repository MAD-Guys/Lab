package it.polito.mad.lab2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button

class ShowProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        val addFriendButton: Button = findViewById(R.id.button_add_friend)
        val messageButton: Button = findViewById(R.id.button_message)

        addFriendButton.setOnClickListener() {
            println("ADD FRIEND BUTTON CLICKED!!!")
        }

        messageButton.setOnClickListener() {
            println("MESSAGE BUTTON CLICKED!!!")
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.edit_button) {
            println("PENCIL CLICKED!!!")
        }

        return super.onOptionsItemSelected(item)
    }

}