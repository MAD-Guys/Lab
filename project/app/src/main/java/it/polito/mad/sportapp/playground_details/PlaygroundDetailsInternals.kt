package it.polito.mad.sportapp.playground_details

import android.content.Intent
import android.graphics. drawable.Drawable
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import it.polito.mad.sportapp.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal fun PlaygroundDetailsFragment.retrieveViews() {
    scrollView = requireView().findViewById(R.id.playgroundScrollView)
    progressBar = requireView().findViewById(R.id.progressBar)
    playgroundImage = requireView().findViewById(R.id.playgroundImage)
    overallRatingBar = requireView().findViewById(R.id.overallRating)
    playgroundName = requireView().findViewById(R.id.playgroundName)
    sportCenterName = requireView().findViewById(R.id.sportCenterName)
    sportEmoji = requireView().findViewById(R.id.sportEmoji)
    playgroundSport = requireView().findViewById(R.id.playgroundSport)
    playgroundAddress = requireView().findViewById(R.id.playgroundAddress)
    playgroundPhoneNumber = requireView().findViewById(R.id.playgroundPhoneNumber)
    playgroundOpeningTime = requireView().findViewById(R.id.playgroundOpeningTime)
    playgroundClosingTime = requireView().findViewById(R.id.playgroundClosingTime)
    playgroundPrice = requireView().findViewById(R.id.playgroundPrice)
    playgroundQualityRatingBar = requireView().findViewById(R.id.overallQualityRatingBar)
    playgroundFacilitiesRatingBar = requireView().findViewById(R.id.overallFacilitiesRatingBar)
    noQualityRatingMessage = requireView().findViewById(R.id.noQualityRatingMessage)
    noFacilitiesRatingMessage = requireView().findViewById(R.id.noFacilitiesRatingMessage)
    yourReviewContainer = requireView().findViewById(R.id.yourReviewContainer)
    reviewList = requireView().findViewById(R.id.reviews_recyclerView)
    addReservationButton = requireView().findViewById(R.id.buttonAddReservation)
    directionsButton = requireView().findViewById(R.id.buttonDirections)
    equipmentsSection = requireView().findViewById(R.id.equipmentsSection)
    equipments = requireView().findViewById(R.id.equipmentsListContainer)
}

internal fun PlaygroundDetailsFragment.initViews() {
    chooseImage(playgroundImage)
    overallRatingBar.rating = viewModel.playground.value?.overallRating!!
    playgroundName.text = viewModel.playground.value?.playgroundName
    sportCenterName.text = viewModel.playground.value?.sportCenterName
    sportEmoji.text = viewModel.playground.value?.sportEmoji
    playgroundSport.text = viewModel.playground.value?.sportName
    playgroundAddress.text = viewModel.playground.value?.sportCenterAddress
    playgroundPhoneNumber.text = viewModel.playground.value?.sportCenterPhoneNumber
    playgroundOpeningTime.text = viewModel.playground.value?.openingHours!!.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    playgroundClosingTime.text = viewModel.playground.value?.closingHours!!.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    playgroundPrice.text = String.format("%.2f", viewModel.playground.value?.pricePerHour)
    playgroundQualityRatingBar.rating = viewModel.playground.value?.overallQualityRating!!
    playgroundFacilitiesRatingBar.rating = viewModel.playground.value?.overallFacilitiesRating!!

    addReservationButton.setOnClickListener { handleAddReservationButton() }
    directionsButton.setOnClickListener { handleDirectionsButton() }


    //hide the rating bars when no evaluation are available
    if(viewModel.playground.value?.overallRating == 0f){
        overallRatingBar.visibility = RatingBar.GONE
    } else {
        overallRatingBar.visibility = RatingBar.VISIBLE
    }
    if(viewModel.playground.value?.overallQualityRating == 0f){
        playgroundQualityRatingBar.visibility = RatingBar.GONE
        noQualityRatingMessage.visibility = TextView.VISIBLE
    } else {
        playgroundQualityRatingBar.visibility = RatingBar.VISIBLE
        noQualityRatingMessage.visibility = TextView.GONE
    }
    if(viewModel.playground.value?.overallFacilitiesRating == 0f){
        playgroundFacilitiesRatingBar.visibility = RatingBar.GONE
        noFacilitiesRatingMessage.visibility = TextView.VISIBLE
    } else {
        playgroundFacilitiesRatingBar.visibility = RatingBar.VISIBLE
        noFacilitiesRatingMessage.visibility = TextView.GONE
    }
}

internal fun PlaygroundDetailsFragment.initEquipments() {
    if(viewModel.equipments.value == null || viewModel.equipments.value!!.isEmpty()){
        equipmentsSection.visibility = LinearLayout.GONE
    } else {
        equipments.removeAllViewsInLayout()
        for ((index, e) in viewModel.equipments.value!!.withIndex()) {
            val row = layoutInflater.inflate(R.layout.equipment_list_item, equipments, false)
            row.id = index
            val equipmentName = row.findViewById<TextView>(R.id.equipment_name)
            // val equipmentQuantity = row.findViewById<TextView>(R.id.equipment_quantity)
            val equipmentPrice = row.findViewById<TextView>(R.id.equipment_unit_price)

            equipmentName.text = e.name
            // equipmentQuantity.text = String.format("%d", e.availability)
            equipmentPrice.text = String.format("%.2f", e.unitPrice)

            equipments.addView(row, index)
        }
    }
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
    lateinit var image: android.graphics.drawable.Drawable
    when (viewModel.playground.value?.sportId) {
        "x7f9jrM9BTiMoIFoyVFq" -> image = ResourcesCompat.getDrawable(resources, R.drawable._01_tennis, null)!!
        "RQgUy37JaJcE8uRmLanb" -> image = ResourcesCompat.getDrawable(resources, R.drawable._02_table_tennis, null)!!
        "fpkrSYDrMUDdqZ4kPfOc" -> image = ResourcesCompat.getDrawable(resources, R.drawable._03_padel, null)!!
        "ZoasHiiaJ3CoNWMEr3RF" -> image = ResourcesCompat.getDrawable(resources, R.drawable._04_basket, null)!!
        "te2BgJjzIJbC9qTgLrT4" -> image = ResourcesCompat.getDrawable(resources, R.drawable._05_football11, null)!!
        "dU8Nvc3SfXfYaQKYzRbr" -> image = ResourcesCompat.getDrawable(resources, R.drawable._06_volleyball, null)!!
        "plGE1kMDKhqE17Azvdw8" -> image = ResourcesCompat.getDrawable(resources, R.drawable._07_beach_volley, null)!!
        "7AIqD0iwHOW6FIycvlwo" -> image = ResourcesCompat.getDrawable(resources, R.drawable._08_football5, null)!!
        "qrwiJsMOa3eCiq6fwOW2" -> image = ResourcesCompat.getDrawable(resources, R.drawable._09_football8, null)!!
        "4nFO9rfxo6iIJVTluCcn" -> image = ResourcesCompat.getDrawable(resources, R.drawable._10_minigolf, null)!!
    }

    view.setImageDrawable(image)
}

internal fun PlaygroundDetailsFragment.handleAddReservationButton() {
    val params = selectedSlotInPlaygroundAvailabilities?.let {
        bundleOf(
            "reservation" to bundleOf(
                "start_slot" to it,
                "slot_duration_mins" to 30,
                "playground_id" to viewModel.playground.value?.playgroundId,
                "playground_name" to viewModel.playground.value?.playgroundName,
                "sport_id" to viewModel.playground.value?.sportId,
                "sport_emoji" to viewModel.playground.value?.sportEmoji,
                "sport_name" to viewModel.playground.value?.sportName,
                "sport_center_id" to viewModel.playground.value?.sportCenterId,
                "sport_center_name" to viewModel.playground.value?.sportCenterName,
                "sport_center_address" to viewModel.playground.value?.sportCenterAddress,
                "playground_price_per_hour" to viewModel.playground.value?.pricePerHour,
            )
        )
    }

    // navigate to the Add Reservation view
    navController.navigate(
        R.id.action_PlaygroundDetailsFragment_to_playgroundAvailabilitiesFragment, params)
}

internal fun PlaygroundDetailsFragment.handleDirectionsButton() {
    val formattedAddress = playgroundAddress.text
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$formattedAddress")
    )
    startActivity(intent)
}