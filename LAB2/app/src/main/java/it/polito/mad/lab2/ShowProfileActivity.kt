package it.polito.mad.lab2

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import es.dmoral.toasty.Toasty
import org.json.JSONObject

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

    // Sport views
    private lateinit var soccer5Chip: Chip
    private lateinit var padelChip: Chip
    private lateinit var miniGolfChip: Chip
    private lateinit var tennisChip: Chip
    private lateinit var soccer11Chip: Chip
    private lateinit var soccer8Chip: Chip
    private lateinit var volleyballChip: Chip
    private lateinit var beachVolleyChip: Chip
    private lateinit var tableTennisChip: Chip
    private lateinit var basketChip: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        // configure toasts appearance
        Toasty.Config.getInstance()
            .allowQueue(true) // optional (prevents several Toastys from queuing)
            .setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100) // optional (set toast gravity, offsets are optional)
            .supportDarkTheme(true) // optional (whether to support dark theme or not)
            .setRTL(true) // optional (icon is on the right)
            .apply() // required

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

        // retrieve buttons and set their callbacks
        addFriendButton = findViewById(R.id.button_add_friend)
        messageButton = findViewById(R.id.button_message)

        addFriendButton.setOnClickListener {
            showToasty("info", this,"Add friend button clicked!!!")
        }

        messageButton.setOnClickListener {
            showToasty("info", this, "Message button clicked!!!")
        }

        // retrieve sport chips
        soccer5Chip = findViewById(R.id.soccer5Chip)
        padelChip = findViewById(R.id.padelChip)
        miniGolfChip = findViewById(R.id.miniGolfChip)
        tennisChip = findViewById(R.id.tennisChip)
        soccer11Chip = findViewById(R.id.soccer11Chip)
        soccer8Chip = findViewById(R.id.soccer8Chip)
        volleyballChip = findViewById(R.id.volleyballChip)
        beachVolleyChip = findViewById(R.id.beachVolleyChip)
        tableTennisChip = findViewById(R.id.tableTennisChip)
        basketChip = findViewById(R.id.basketChip)
    }

    override fun onResume() {
        super.onResume()

        /*  The information is retrieved and showed in onResume() because  *
         *  it has to be refreshed after saving in edit mode (in fact,     *
         *  when the EditProfileActivity is created and started, the       *
         *  ShowProfileActivity is not destroyed, but it is still behind) */

        // retrieve data from SharedPreferences
        val sh = getSharedPreferences("it.polito.mad.lab2", MODE_PRIVATE)
        val jsonObjectProfile: JSONObject? = sh.getString("profile", null)?.let { JSONObject(it) }

        // retrieve user info from JSON object (if any, or take the default one) and update view's texts
        firstName.text = jsonObjectProfile?.getString("firstName") ?: getString(R.string.first_name)
        lastName.text = jsonObjectProfile?.getString("lastName") ?: getString(R.string.last_name)
        username.text = jsonObjectProfile?.getString("username") ?: getString(R.string.username)
        gender.text = jsonObjectProfile?.getString("gender") ?: getString(R.string.user_gender)
        age.text = jsonObjectProfile?.getString("age") ?: getString(R.string.user_age)
        location.text =
            jsonObjectProfile?.getString("location") ?: getString(R.string.user_location)
        bio.text = jsonObjectProfile?.getString("bio") ?: getString(R.string.user_bio)

        // retrieve profile and background picture from the internal storage
        val profilePictureBitmap = getPictureFromInternalStorage(filesDir, "profilePicture.jpeg")
        val backgroundProfilePictureBitmap =
            getPictureFromInternalStorage(filesDir, "backgroundProfilePicture.jpeg")

        // update profile and background picture with the ones uploaded by the user, if any
        profilePictureBitmap?.let { profilePicture.setImageBitmap(it) }
        backgroundProfilePictureBitmap?.let { backgroundProfilePicture.setImageBitmap(it) }

        // retrieve sports from storage and set them properly based on their respective level

        setSportBadge("basket", basketChip, jsonObjectProfile)
        setSportBadge("soccer11", soccer11Chip, jsonObjectProfile)
        setSportBadge("soccer5", soccer5Chip, jsonObjectProfile)
        setSportBadge("soccer8", soccer8Chip, jsonObjectProfile)
        setSportBadge("tennis", tennisChip, jsonObjectProfile)
        setSportBadge("volleyball", volleyballChip, jsonObjectProfile)
        setSportBadge("tableTennis", tableTennisChip, jsonObjectProfile)
        setSportBadge("beachVolley", beachVolleyChip, jsonObjectProfile)
        setSportBadge("padel", padelChip, jsonObjectProfile)
        setSportBadge("miniGolf", miniGolfChip, jsonObjectProfile)

        if (jsonObjectProfile == null) { // first time the app is launched
            loadHardcodedSports() //Some sports will appear the first time
        }
    }

    private fun setSportBadge(sportName: String, sportChip: Chip, jsonObjectProfile: JSONObject?) {
        // hide the sport badge
        sportChip.visibility = Chip.GONE

        // retrieve sport persistent data, if any
        val sport: Sport? = jsonObjectProfile?.optJSONObject(sportName)?.run {
            Sport(this.getBoolean("selected"), this.getInt("level"))
        }

        // if the sport has been selected, choose and set the right level icon
        if (sport?.selected == true) {
            // show the sport badge
            sportChip.visibility = Chip.VISIBLE

            when(sport.level) {
                0 -> {
                    sportChip.chipIcon = ContextCompat.getDrawable(this, R.drawable.beginner_level_badge)
                    sportChip.setTextColor(getColor(R.color.beginner_badge_blue))
                    sportChip.setChipStrokeColorResource(R.color.beginner_badge_blue)
                }
                1 -> {
                    sportChip.chipIcon = ContextCompat.getDrawable(this, R.drawable.intermediate_level_badge)
                    sportChip.setTextColor(getColor(R.color.intermediate_badge_blue))
                    sportChip.setChipStrokeColorResource(R.color.intermediate_badge_blue)
                }
                2 -> {
                    sportChip.chipIcon = ContextCompat.getDrawable(this, R.drawable.expert_level_badge)
                    sportChip.setTextColor(getColor(R.color.expert_badge_blue))
                    sportChip.setChipStrokeColorResource(R.color.expert_badge_blue)
                }
                3 -> {
                    sportChip.chipIcon = ContextCompat.getDrawable(this, R.drawable.pro_level_badge)
                    sportChip.setTextColor(getColor(R.color.pro_badge_grey))
                    sportChip.setChipStrokeColorResource(R.color.pro_badge_grey)
                }
            }
        }

    }

    private fun loadHardcodedSports() {
        basketChip.chipIcon = ContextCompat.getDrawable(this, R.drawable.expert_level_badge)
        basketChip.setTextColor(getColor(R.color.expert_badge_blue))
        basketChip.setChipStrokeColorResource(R.color.expert_badge_blue)
        basketChip.visibility = Chip.VISIBLE

        tennisChip.chipIcon = ContextCompat.getDrawable(this, R.drawable.beginner_level_badge)
        tennisChip.setTextColor(getColor(R.color.beginner_badge_blue))
        tennisChip.setChipStrokeColorResource(R.color.beginner_badge_blue)
        tennisChip.visibility = Chip.VISIBLE
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