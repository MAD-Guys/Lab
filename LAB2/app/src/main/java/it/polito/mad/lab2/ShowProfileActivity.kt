package it.polito.mad.lab2

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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
    private lateinit var sportChips: MutableMap<String,Chip>
    // sport data
    private lateinit var sportData: MutableMap<String,Sport>

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


        /* manage sports */
        sportChips = HashMap()
        sportData = HashMap()

        // retieve and clean sports container
        val sportsContainer = findViewById<ChipGroup>(R.id.sports_container)
        sportsContainer.removeAllViews()

        // first time the app is launched, some hardcoded sports will appear
        if (jsonObjectProfile == null)
            loadHardcodedSports(*getHardcodedSports(), parent=sportsContainer)
        else {
            // load the (already) selected sports by the user
            val sportJson = jsonObjectProfile.getJSONObject("sports")
            for (sportName in sportJson.keys()) {
                val sport = Sport.from(sportName, sportJson)

                if(sport.selected) {
                    // create sport chip
                    val sportChip = createSportChip(sport, sportsContainer)

                    // save chip and information
                    sportChips[sportName] = sportChip
                    sportData[sportName] = sport
                }
            }
        }

        // display sports in decreasing order of level
        sportChips.asSequence().sortedByDescending {(sportName, _) ->
            sportData[sportName]!!.level
        }.forEach { (_, chip) ->
            sportsContainer.addView(chip)
        }
    }

    private fun createSportChip(sport: Sport, parent: ViewGroup): Chip {
        val chip = layoutInflater.inflate(R.layout.show_profile_chip, parent, false) as Chip

        chip.apply {
            setVisible(sport.selected)  // !!!
            text = extendedNameOf(sport.name) // !!!
            // set level characteristics
            when(sport.level) {
                Level.BEGINNER -> {
                    setChipIconResource(R.drawable.beginner_level_badge)
                    setChipStrokeColorResource(R.color.beginner_badge_blue)
                    setTextColor(getColor(R.color.beginner_badge_blue))
                }
                Level.INTERMEDIATE -> {
                    setChipIconResource(R.drawable.intermediate_level_badge)
                    setChipStrokeColorResource(R.color.intermediate_badge_blue)
                    setTextColor(getColor(R.color.intermediate_badge_blue))
                    setChipIconSizeResource(R.dimen.chip_icon_size_big)
                }
                Level.EXPERT -> {
                    setChipIconResource(R.drawable.expert_level_badge)
                    setChipStrokeColorResource(R.color.expert_badge_blue)
                    setTextColor(getColor(R.color.expert_badge_blue))
                }
                Level.PRO -> {
                    setChipIconResource(R.drawable.pro_level_badge)
                    setChipStrokeColorResource(R.color.pro_badge_grey)
                    setTextColor(getColor(R.color.pro_badge_grey))
                }
                else -> throw RuntimeException("Unexpected selected sport with NO_LEVEL!")
            }
        }

        return chip
    }

    private fun loadHardcodedSports(vararg hardcodedSports: Sport, parent: ViewGroup) {
        hardcodedSports.forEach {
            // create chip view
            val sportChip = createSportChip(it, parent)

            // save chip and sport info
            sportChips[it.name] = sportChip
            sportData[it.name] = it
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