package it.polito.mad.lab2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast

class ShowProfileActivity : AppCompatActivity() {

    private lateinit var addFriendButton: Button
    private lateinit var messageButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        addFriendButton = findViewById(R.id.button_add_friend)
        messageButton = findViewById(R.id.button_message)

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

            //Launching the EditProfileActivity
            val intent: Intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)

            val toast: Toast = Toast.makeText(this, "Now you are in EDIT mode!", Toast.LENGTH_SHORT)
            toast.show()
        }

        return super.onOptionsItemSelected(item)
    }

}