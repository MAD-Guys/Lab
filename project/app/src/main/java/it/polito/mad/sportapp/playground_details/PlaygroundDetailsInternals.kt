package it.polito.mad.sportapp.playground_details

import android.content.Intent
import android.graphics. drawable.Drawable
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import it.polito.mad.sportapp.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal fun PlaygroundDetailsFragment.retrieveViews() {
    playgroundImage = requireView().findViewById(R.id.playgroundImage)
    overallRatingBar = requireView().findViewById(R.id.overallRating)
    playgroundName = requireView().findViewById(R.id.playgroundName)
    sportCenterName = requireView().findViewById(R.id.sportCenterName)
    sportEmoji = requireView().findViewById(R.id.sportEmoji)
    playgroundSport = requireView().findViewById(R.id.playgroundSport)
    playgroundAddress = requireView().findViewById(R.id.playgroundAddress)
    playgroundOpeningTime = requireView().findViewById(R.id.playgroundOpeningTime)
    playgroundClosingTime = requireView().findViewById(R.id.playgroundClosingTime)
    playgroundPrice = requireView().findViewById(R.id.playgroundPrice)
    playgroundQualityRatingBar = requireView().findViewById(R.id.overallQualityRatingBar)
    playgroundFacilitiesRatingBar = requireView().findViewById(R.id.overallFacilitiesRatingBar)
    yourReviewContainer = requireView().findViewById(R.id.yourReviewContainer)
    reviewList = requireView().findViewById(R.id.reviews_recyclerView)
    addReservationButton = requireView().findViewById(R.id.buttonAddReservation)
    directionsButton = requireView().findViewById(R.id.buttonDirections)
}

internal fun PlaygroundDetailsFragment.initViews() {
    chooseImage(playgroundImage)
    overallRatingBar.rating = viewModel.playground.value?.overallRating!!
    playgroundName.text = viewModel.playground.value?.playgroundName
    sportCenterName.text = viewModel.playground.value?.sportCenterName
    sportEmoji.text = viewModel.playground.value?.sportEmoji
    playgroundSport.text = viewModel.playground.value?.sportName
    playgroundAddress.text = viewModel.playground.value?.sportCenterAddress
    playgroundOpeningTime.text = viewModel.playground.value?.openingHours!!.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    playgroundClosingTime.text = viewModel.playground.value?.closingHours!!.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    playgroundPrice.text = String.format("%.2f", viewModel.playground.value?.pricePerHour)
    playgroundQualityRatingBar.rating = viewModel.playground.value?.overallQualityRating!!
    playgroundFacilitiesRatingBar.rating = viewModel.playground.value?.overallFacilitiesRating!!

    addReservationButton.setOnClickListener { handleAddReservationButton() }
    directionsButton.setOnClickListener { handleDirectionsButton() }
}

// manage menu item selection
internal fun PlaygroundDetailsFragment.menuInit() {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.playground_details_menu, menu)

            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
                it.title = "Playground Details"
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // handle the menu selection
            return when (menuItem.itemId) {
                R.id.add_reservation_button -> {
                    handleAddReservationButton()
                    true
                }

                else -> false
            }
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

internal fun PlaygroundDetailsFragment.chooseImage(view: ImageView) {
    lateinit var image: Drawable
    when (viewModel.playground.value?.sportId) {
        1 -> image = ResourcesCompat.getDrawable(resources, R.drawable._01_tennis, null)!!
        2 -> image = ResourcesCompat.getDrawable(resources, R.drawable._02_table_tennis, null)!!
        3 -> image = ResourcesCompat.getDrawable(resources, R.drawable._03_padel, null)!!
        4 -> image = ResourcesCompat.getDrawable(resources, R.drawable._04_basket, null)!!
        5 -> image = ResourcesCompat.getDrawable(resources, R.drawable._05_football11, null)!!
        6 -> image = ResourcesCompat.getDrawable(resources, R.drawable._06_volleyball, null)!!
        7 -> image = ResourcesCompat.getDrawable(resources, R.drawable._07_beach_volley, null)!!
        8 -> image = ResourcesCompat.getDrawable(resources, R.drawable._08_football5, null)!!
        9 -> image = ResourcesCompat.getDrawable(resources, R.drawable._09_football8, null)!!
        10 -> image = ResourcesCompat.getDrawable(resources, R.drawable._10_minigolf, null)!!
    }

    view.setImageDrawable(image)
}

internal fun PlaygroundDetailsFragment.handleAddReservationButton() {
    // navigate to the Add Reservation view
    navController.navigate(
        R.id.action_PlaygroundDetailsFragment_to_playgroundAvailabilitiesFragment)
}

internal fun PlaygroundDetailsFragment.handleDirectionsButton() {
    val formattedAddress = playgroundAddress.text
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$formattedAddress")
    )
    startActivity(intent)
}