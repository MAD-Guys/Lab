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

//import com.google.android.material.chip.Chip

class ShowProfileActivity : AppCompatActivity() {

    //General info variables
    private lateinit var fullName: TextView
    private lateinit var nickName: TextView
    private lateinit var sex: TextView
    private lateinit var age: TextView
    private lateinit var location: TextView
    private lateinit var bio: TextView

    //Profile picture variables
    private lateinit var profilePicture: ImageView

    //Button variables
    private lateinit var addFriendButton: Button
    private lateinit var messageButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        //Sport chips variables
        // private lateinit var chip: Chip
        // chip = findViewById(R.id.chipSport1)
        // chip.visibility = Chip.GONE

        fullName = findViewById(R.id.fullName)
        nickName = findViewById(R.id.nickname)
        sex = findViewById(R.id.sex)
        age = findViewById(R.id.age)
        location = findViewById(R.id.location)
        bio = findViewById(R.id.bio)

        //Managing the profile picture
        profilePicture = findViewById(R.id.profile_picture)

        // retrieve buttons, set callbacks and text
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

    override fun onResume() {
        super.onResume()

        // retrieve data from SharedPreferences
        val sh = getSharedPreferences("it.polito.mad.lab2", Context.MODE_PRIVATE)

        val firstNameResume = sh.getString("firstName", getString(R.string.first_name))
        val lastNameResume = sh.getString("lastName", getString(R.string.last_name))
        val nicknameResume = sh.getString("nickname", getString(R.string.nickname))
        val sexResume = sh.getString("sex", getString(R.string.sex))
        val ageResume = sh.getString("age", getString(R.string.age))
        val locationResume = sh.getString("location", getString(R.string.location))
        val bioResume = sh.getString("bio", getString(R.string.bio))

        val profilePictureResume = sh.getString("profilePicture", null)

        fullName.text = "$firstNameResume $lastNameResume"
        nickName.text = "@$nicknameResume"
        sex.text = sexResume
        age.text = ageResume
        location.text = locationResume
        bio.text = bioResume

        if (profilePictureResume != null && !profilePictureResume.equals("", ignoreCase = true)) {
            val b: ByteArray = Base64.decode(profilePictureResume, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
            profilePicture.setImageBitmap(bitmap)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.profile_menu, menu)
        // change app bar's title
        supportActionBar?.title = "Profile"
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

        val toast = Toast.makeText(this, "Now you are in EDIT mode!", Toast.LENGTH_SHORT)
        toast.show()
        return true
    }

}