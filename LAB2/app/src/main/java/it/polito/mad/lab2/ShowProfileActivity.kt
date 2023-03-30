package it.polito.mad.lab2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

//import com.google.android.material.chip.Chip

class ShowProfileActivity : AppCompatActivity() {
    private lateinit var addFriendButton: Button
    private lateinit var messageButton: Button
    private lateinit var fullname: TextView
    private lateinit var nickname: TextView
    private lateinit var sex: TextView
    private lateinit var age: TextView
    private lateinit var bio: TextView
    private lateinit var location: TextView
    // private lateinit var chip: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        // manage sports chips
        // chip = findViewById(R.id.chipSport1)
        // chip.visibility = Chip.GONE

        // retrieve buttons, set callbacks and text
        addFriendButton = findViewById(R.id.button_add_friend)
        messageButton = findViewById(R.id.button_message)

        fullname = findViewById(R.id.fullName)
        nickname = findViewById(R.id.nickname)
        sex = findViewById(R.id.sex)
        age = findViewById(R.id.age)
        bio = findViewById(R.id.bio)
        location = findViewById(R.id.location)

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
        val _firstName = sh.getString("firstName", getString(R.string.first_name))
        val _lastName = sh.getString("lastName", getString(R.string.last_name))
        val _sex = sh.getString("sex", getString(R.string.sex))
        val _age = sh.getString("age", getString(R.string.age))
        val _nickname = sh.getString("nickname", getString(R.string.nickname))
        val _bio = sh.getString("bio", getString(R.string.bio))
        val _location = sh.getString("location",getString(R.string.location))

        fullname.text = "$_firstName $_lastName"
        nickname.text = _nickname
        age.text = _age
        sex.text = _sex
        bio.text = _bio
        location.text = _location

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