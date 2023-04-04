package it.polito.mad.lab2

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.chip.Chip
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
    private lateinit var basketChip: Chip
    private lateinit var football11Chip: Chip
    private lateinit var football5Chip: Chip
    private lateinit var football8Chip: Chip
    private lateinit var tennisChip: Chip
    private lateinit var volleyballChip: Chip
    private lateinit var tableTennisChip: Chip
    private lateinit var beachVolleyChip: Chip
    private lateinit var padelChip: Chip
    private lateinit var miniGolfChip: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

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

        // retrieve sport chips
        basketChip = findViewById(R.id.basketChip)
        football11Chip = findViewById(R.id.football11Chip)
        football5Chip = findViewById(R.id.football5Chip)
        football8Chip = findViewById(R.id.football8Chip)
        tennisChip = findViewById(R.id.tennisChip)
        volleyballChip = findViewById(R.id.volleyballChip)
        tableTennisChip = findViewById(R.id.tableTennisChip)
        beachVolleyChip = findViewById(R.id.beachVolleyChip)
        padelChip = findViewById(R.id.padelChip)
        miniGolfChip = findViewById(R.id.miniGolfChip)

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

        // the information retrieval is done in onResume() because information has to be refreshed
        // after saving (when the EditProfileActivity is created and started, the ShowProfileActivity is not destroyed)

        /* update shown user info and pictures */

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

        // retrieve profile picture from the internal storage
        val profilePictureBitmap = getPictureFromInternalStorage(filesDir, "profilePicture.jpeg")
        val backgroundProfilePictureBitmap =
            getPictureFromInternalStorage(filesDir, "backgroundProfilePicture.jpeg")

        // update profile and background picture with the ones uploaded by the user, if any
        profilePictureBitmap?.let { profilePicture.setImageBitmap(it) }
        backgroundProfilePictureBitmap?.let { backgroundProfilePicture.setImageBitmap(it) }

        // retrieve sports
        val basketJSON: JSONObject? = jsonObjectProfile?.optJSONObject("basket")
        var basketResume: Sport? = null
        if (basketJSON != null) basketResume =
            Sport(basketJSON.getBoolean("selected"), basketJSON.getInt("level"))
        basketChip.visibility = Chip.GONE
        if (basketResume != null && basketResume.selected) {
            basketChip.visibility = Chip.VISIBLE
            when(basketResume.level){
                0 -> basketChip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> basketChip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> basketChip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> basketChip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
        }

        val football11JSON: JSONObject? = jsonObjectProfile?.optJSONObject("football11")
        var football11Resume: Sport? = null
        if (football11JSON != null) football11Resume =
            Sport(football11JSON.getBoolean("selected"), football11JSON.getInt("level"))
        football11Chip.visibility = Chip.GONE
        if (football11Resume != null && football11Resume.selected) {
            football11Chip.visibility = Chip.VISIBLE
            when(football11Resume.level){
                0 -> football11Chip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> football11Chip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> football11Chip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> football11Chip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
        }

        val football5JSON: JSONObject? = jsonObjectProfile?.optJSONObject("football5")
        var football5Resume: Sport? = null
        if (football5JSON != null) football5Resume =
            Sport(football5JSON.getBoolean("selected"), football5JSON.getInt("level"))
        football5Chip.visibility = Chip.GONE
        if (football5Resume != null && football5Resume.selected) {
            football5Chip.visibility = Chip.VISIBLE
            when(football5Resume.level){
                0 -> football5Chip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> football5Chip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> football5Chip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> football5Chip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
        }

        val football8JSON: JSONObject? = jsonObjectProfile?.optJSONObject("football8")
        var football8Resume: Sport? = null
        if (football8JSON != null) football8Resume =
            Sport(football8JSON.getBoolean("selected"), football8JSON.getInt("level"))
        football8Chip.visibility = Chip.GONE
        if (football8Resume != null && football8Resume.selected) {
            football8Chip.visibility = Chip.VISIBLE
            when(football8Resume.level){
                0 -> football8Chip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> football8Chip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> football8Chip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> football8Chip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
        }

        val tennisJSON: JSONObject? = jsonObjectProfile?.optJSONObject("tennis")
        var tennisResume: Sport? = null
        if (tennisJSON != null) tennisResume =
            Sport(tennisJSON.getBoolean("selected"), tennisJSON.getInt("level"))
        tennisChip.visibility = Chip.GONE
        if (tennisResume != null && tennisResume.selected) {
            tennisChip.visibility = Chip.VISIBLE
            when(tennisResume.level){
                0 -> tennisChip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> tennisChip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> tennisChip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> tennisChip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
        }

        val volleyballJSON: JSONObject? = jsonObjectProfile?.optJSONObject("volleyball")
        var volleyballResume: Sport? = null
        if (volleyballJSON != null) volleyballResume =
            Sport(volleyballJSON.getBoolean("selected"), volleyballJSON.getInt("level"))
        volleyballChip.visibility = Chip.GONE
        if (volleyballResume != null && volleyballResume.selected) {
            volleyballChip.visibility = Chip.VISIBLE
            when(volleyballResume.level){
                0 -> volleyballChip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> volleyballChip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> volleyballChip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> volleyballChip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
        }

        val tableTennisJSON: JSONObject? = jsonObjectProfile?.optJSONObject("tableTennis")
        var tableTennisResume: Sport? = null
        if (tableTennisJSON != null) tableTennisResume =
            Sport(tableTennisJSON.getBoolean("selected"), tableTennisJSON.getInt("level"))
        tableTennisChip.visibility = Chip.GONE
        if (tableTennisResume != null && tableTennisResume.selected) {
            tableTennisChip.visibility = Chip.VISIBLE
            when(tableTennisResume.level){
                0 -> tableTennisChip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> tableTennisChip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> tableTennisChip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> tableTennisChip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
        }

        val beachVolleyJSON: JSONObject? = jsonObjectProfile?.optJSONObject("beachVolley")
        var beachVolleyResume: Sport? = null
        if (beachVolleyJSON != null) beachVolleyResume =
            Sport(beachVolleyJSON.getBoolean("selected"), beachVolleyJSON.getInt("level"))
        beachVolleyChip.visibility = Chip.GONE
        if (beachVolleyResume != null && beachVolleyResume.selected) {
            beachVolleyChip.visibility = Chip.VISIBLE
            when(beachVolleyResume.level){
                0 -> beachVolleyChip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> beachVolleyChip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> beachVolleyChip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> beachVolleyChip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
        }

        val padelJSON: JSONObject? = jsonObjectProfile?.optJSONObject("padel")
        var padelResume: Sport? = null
        if (padelJSON != null) padelResume =
            Sport(padelJSON.getBoolean("selected"), padelJSON.getInt("level"))
        padelChip.visibility = Chip.GONE
        if (padelResume != null && padelResume.selected) {
            padelChip.visibility = Chip.VISIBLE
            when(padelResume.level){
                0 -> padelChip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> padelChip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> padelChip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> padelChip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
        }

        val miniGolfJSON: JSONObject? = jsonObjectProfile?.optJSONObject("miniGolf")
        var miniGolfResume: Sport? = null
        if (miniGolfJSON != null) miniGolfResume =
            Sport(miniGolfJSON.getBoolean("selected"), miniGolfJSON.getInt("level"))
        miniGolfChip.visibility = Chip.GONE
        if (miniGolfResume != null && miniGolfResume.selected) {
            miniGolfChip.visibility = Chip.VISIBLE
            when(miniGolfResume.level){
                0 -> miniGolfChip.chipIcon = getDrawable(R.drawable.beginner_level_badge)
                1 -> miniGolfChip.chipIcon = getDrawable(R.drawable.intermediate_level_badge)
                2 -> miniGolfChip.chipIcon = getDrawable(R.drawable.expert_level_badge)
                3 -> miniGolfChip.chipIcon = getDrawable(R.drawable.pro_level_badge)
            }
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