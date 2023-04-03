package it.polito.mad.lab2

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout


class ShowProfileActivity : AppCompatActivity() {
    // User info views
    private lateinit var firstName: TextView
    private lateinit var lastName: TextView
    private lateinit var username: TextView
    private lateinit var gender: TextView
    private lateinit var age: TextView
    private lateinit var location: TextView
    private lateinit var bio: TextView

    // Profile picture views
    private lateinit var profilePicture: ImageView
    private lateinit var backgroundProfilePicture: ImageView

    // Button views
    private lateinit var addFriendButton: Button
    private lateinit var messageButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        // Sport chips variables
        // TODO

        // retrieve user info and picture views
        firstName = findViewById(R.id.first_name)
        lastName = findViewById(R.id.last_name)
        username = findViewById(R.id.username)
        gender = findViewById(R.id.user_gender)
        age = findViewById(R.id.user_age)
        location = findViewById(R.id.user_location)
        bio = findViewById(R.id.user_bio)
        profilePicture = findViewById(R.id.profile_picture)
        backgroundProfilePicture = findViewById(R.id.background_profile_picture)

        // retrieve buttons, set callbacks and text
        addFriendButton = findViewById(R.id.button_add_friend)
        messageButton = findViewById(R.id.button_message)

        addFriendButton.setOnClickListener {
            val toast = Toast.makeText(this, "Add friend button clicked!!!", Toast.LENGTH_SHORT)
            toast.show()
        }

        messageButton.setOnClickListener {
            val toast = Toast.makeText(this, "Message button clicked!!!", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    override fun onResume() {
        super.onResume()

        // retrieve shared preferences object
        val sh = getSharedPreferences("it.polito.mad.lab2", Context.MODE_PRIVATE)

        // retrieve data from SharedPreferences, if any, or take a default one
        val firstNameResume = sh.getString("firstName", getString(R.string.first_name))
        val lastNameResume = sh.getString("lastName", getString(R.string.last_name))
        val nicknameResume = sh.getString("username", getString(R.string.username))
        val genderResume = sh.getString("gender", getString(R.string.user_gender))
        val ageResume = sh.getString("age", getString(R.string.user_age))
        val locationResume = sh.getString("location", getString(R.string.user_location))
        val bioResume = sh.getString("bio", getString(R.string.user_bio))

        // retrieve profile picture from the internal storage
        val profilePictureResume = getProfilePictureFromInternalStorage(filesDir)

        // update view's texts with the actual user info
        firstName.text = firstNameResume
        lastName.text = lastNameResume
        username.text = nicknameResume
        gender.text = genderResume
        age.text = ageResume
        location.text = locationResume
        bio.text = bioResume

        // update profile picture with the one uploaded by the user, if any
        if (profilePictureResume != null) {
            profilePicture.setImageBitmap(profilePictureResume)
        }
    }

    /* app menu */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate and render the menu
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.profile_menu, menu)
        // change app bar's title
        supportActionBar?.title = "Profile"

        val menuHeight = supportActionBar?.height!!
        val profilePictureContainer = findViewById<ConstraintLayout>(R.id.profile_picture_container)
        val backgroundProfilePicture = findViewById<ImageView>(R.id.background_profile_picture)
        val profilePicture = findViewById<ImageView>(R.id.profile_picture)

        // set profile picture height 1/3 of the app view
        this.setProfilePictureSize(
            menuHeight,
            profilePictureContainer,
            backgroundProfilePicture,
            profilePicture
        )

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        // detect which item has been selected and perform corresponding action
        R.id.edit_button -> handleEditButton()
        else -> super.onOptionsItemSelected(item)
    }

    private fun handleEditButton(): Boolean {
        // Launching the EditProfileActivity
        val editProfileIntent = Intent(this, EditProfileActivity::class.java)
        startActivity(editProfileIntent)
        return true
    }
}