package it.polito.mad.sportapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SportAppActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    val db = FirebaseFirestore.getInstance()

    private lateinit var bottomNavigationView: NavigationBarView
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set sport app content view
        setContentView(R.layout.activity_sport_app)

        // set english as default language
        setApplicationLocale(this, "en", "EN")

        // set light theme as default
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        /* bottom bar */

        // initialize bottom navigation view
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar)

        // initialize navigation controller
        navController = (
                supportFragmentManager
                    .findFragmentById(R.id.fragment_container_view) as NavHostFragment
                ).navController

        // set bottom navigation bar listener
        bottomNavigationView.setOnItemSelectedListener(this)

        // configure toasts appearance
        toastyInit()

        //TODO: setup firestore db properly and delete these functions
        tryWriteFirestoreDb()
        tryReadFirestoreDb()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val currentFragment = navController.currentDestination?.id

        when (item.itemId) {

            R.id.reservations -> {
                if (currentFragment != R.id.showReservationsFragment) {
                    navController.navigate(R.id.showReservationsFragment)
                }
                return true
            }

            R.id.playgrounds -> {
                if (currentFragment != R.id.playgroundAvailabilitiesFragment) {
                    navController.navigate(R.id.playgroundAvailabilitiesFragment)
                }
                return true
            }

            R.id.profile -> {
                if (currentFragment != R.id.showProfileFragment) {
                    navController.navigate(R.id.showProfileFragment)
                }
                return true
            }

            else -> return false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_container_view)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun tryWriteFirestoreDb() {

        // Create a new user with a first and last name
        val user = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )

        // Create a new user with a first, middle, and last name
        val user2 = hashMapOf(
            "first" to "Alan",
            "middle" to "Mathison",
            "last" to "Turing",
            "born" to 1912
        )

        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

        // Add a new document with a generated ID
        db.collection("users")
            .add(user2)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }

    private fun tryReadFirestoreDb() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }


}