package it.polito.mad.sportapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.application_utilities.checkIfUserIsLoggedIn
import it.polito.mad.sportapp.application_utilities.setApplicationLocale
import it.polito.mad.sportapp.application_utilities.showToasty
import it.polito.mad.sportapp.application_utilities.toastyInit
import it.polito.mad.sportapp.notifications.manageNotification
import it.polito.mad.sportapp.playgrounds.PlaygroundsViewModel
import it.polito.mad.sportapp.profile.ProfileViewModel
import it.polito.mad.sportapp.show_reservations.ShowReservationsViewModel

@AndroidEntryPoint
class SportAppActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    // request notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            showToasty("info", this, "Permission not granted, notifications will not be received!")
        }
    }

    // activity view model
    private lateinit var activityVm: SportAppViewModel

    // shared instances of fragments view models
    private var profileVm: ProfileViewModel? = null
    private var playgroundsVm: PlaygroundsViewModel? = null
    private var showReservationVm: ShowReservationsViewModel? = null

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

        // initialize activity view model
        activityVm = ViewModelProvider(this)[SportAppViewModel::class.java]

        // initialize fragments view models
        activityVm.isUserLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn && activityVm.areVmInstancesCreated == false) {
                profileVm = ViewModelProvider(this)[ProfileViewModel::class.java]
                playgroundsVm = ViewModelProvider(this)[PlaygroundsViewModel::class.java]
                showReservationVm = ViewModelProvider(this)[ShowReservationsViewModel::class.java]

                // initialize notification list
                activityVm.getUserNotifications()

                activityVm.setVmInstancesCreated()
            }
        }

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

            R.id.slots -> {
                if (currentFragment != R.id.playgroundAvailabilitiesFragment) {
                    navController.navigate(R.id.playgroundAvailabilitiesFragment)
                }
                return true
            }

            R.id.playgrounds -> {
                if (currentFragment != R.id.playgroundsBySportFragment &&
                    currentFragment != R.id.playgroundsByCenterFragment
                ) {
                    navController.navigate(R.id.playgroundsBySportFragment)
                }

                return true
            }

            R.id.notifications -> {
                if (currentFragment != R.id.notificationsFragment) {
                    navController.navigate(R.id.notificationsFragment)
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

    override fun onStart() {
        super.onStart()

        // request notification permission
        askNotificationPermission()
    }

    // manage notification click when the activity instance is already created
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (checkIfUserIsLoggedIn()) {
            if (intent != null) {
                manageNotification(intent, navController)
            } else {
                navController.navigate(R.id.showReservationsFragment)
            }
        } else {

            val bundle = bundleOf("new_intent" to intent)

            navController.navigate(R.id.loginFragment, bundle)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_container_view)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}