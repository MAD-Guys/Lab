package it.polito.mad.sportapp.playground_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.playground_details.reviews_recycler_view.ReviewAdapter

class PlaygroundDetailsFragment : Fragment(R.layout.fragment_playground_details) {

    private val viewModel by viewModels<PlaygroundDetailsViewModel>()

    private var playgroundId = -1

    private lateinit var playgroundImage: ImageView
    private lateinit var overallRatingBar: RatingBar
    private lateinit var playgroundName: TextView
    private lateinit var sportCenterName: TextView
    private lateinit var playgroundSport: TextView
    private lateinit var playgroundAddress: TextView
    private lateinit var playgroundOpeningTime: TextView
    private lateinit var playgroundClosingTime: TextView
    private lateinit var playgroundPrice: TextView
    private lateinit var addReservationButton: Button
    private lateinit var directionsButton: Button


    private lateinit var playgroundQualityRatingBar: RatingBar
    private lateinit var playgroundFacilitiesRatingBar: RatingBar

    private lateinit var yourReviewContainer: LinearLayout
    private lateinit var reviewList: RecyclerView
    private val reviewAdapter = ReviewAdapter()

    private lateinit var yourReview: View
    private lateinit var yourUsername: TextView
    private lateinit var yourReviewDate: TextView
    private lateinit var yourQualityRating: RatingBar
    private lateinit var yourFacilitiesRating: RatingBar
    private lateinit var yourReviewText: TextView
    private lateinit var yourReviewEditText: EditText
    private lateinit var addReviewButton: Button
    private lateinit var editReviewButton: Button
    private lateinit var saveReviewButton: Button
    private lateinit var existingReview: LinearLayout
    private lateinit var writeReview: LinearLayout

    private lateinit var bottomNavigationBar: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        bottomNavigationBar =
            (requireActivity() as AppCompatActivity).findViewById(R.id.bottom_navigation_bar)

        // change app bar's title
        actionBar?.title = "Playground Details"
        bottomNavigationBar.visibility = View.GONE

        // Retrieve event id
        playgroundId = arguments?.getInt("id_playground") ?: -1

        // Retrieve views
        retrieveViews()

        viewModel.playground.observe(viewLifecycleOwner) {
            if (viewModel.playground.value != null) {
                viewModel.setYourReview()
                initViews()
                initYourReview()
                initReviewList()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (playgroundId != -1)
            viewModel.getPlaygroundFromDb(playgroundId)
    }

    override fun onPause() {
        super.onPause()
        bottomNavigationBar.visibility = View.VISIBLE
    }

    private fun retrieveViews() {
        playgroundImage = requireView().findViewById(R.id.playgroundImage)
        overallRatingBar = requireView().findViewById(R.id.overallRating)
        playgroundName = requireView().findViewById(R.id.playgroundName)
        sportCenterName = requireView().findViewById(R.id.sportCenterName)
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
        directionsButton = requireView().findViewById(R.id.directionsButton)
    }

    private fun initViews() {
        chooseImage(playgroundImage)
        overallRatingBar.rating = 3.5f //viewModel.playground.value?.overallRating
        playgroundName.text = viewModel.playground.value?.playgroundName
        sportCenterName.text = viewModel.playground.value?.sportCenterName
        playgroundSport.text = "Basketball" //viewModel.playground.value?.sportName
        playgroundAddress.text =
            "Via Roma 1, Turin" //viewModel.playground.value?.sportCenterAddress
        playgroundOpeningTime.text = viewModel.playground.value?.openingHours
        playgroundClosingTime.text = viewModel.playground.value?.closingHours
        playgroundPrice.text = String.format("%.2f", viewModel.playground.value?.pricePerHour)
        playgroundQualityRatingBar.rating = 4.5f //viewModel.playground.value?.qualityRating
        playgroundFacilitiesRatingBar.rating = 2.5f //viewModel.playground.value?.facilitiesRating

        addReservationButton.setOnClickListener { handleAddReservationButton() }
        directionsButton.setOnClickListener { handleDirectionsButton() }
    }

    private fun initYourReview() {
        yourReview = layoutInflater.inflate(R.layout.your_review, yourReviewContainer)

        //retrieve views
        yourUsername = yourReview.findViewById(R.id.username)
        yourReviewDate = yourReview.findViewById(R.id.date)
        yourQualityRating = yourReview.findViewById(R.id.qualityRatingBar)
        yourFacilitiesRating = yourReview.findViewById(R.id.facilitiesRatingBar)
        yourReviewText = yourReview.findViewById(R.id.reviewBody)
        yourReviewEditText = yourReview.findViewById(R.id.reviewInputBody)
        addReviewButton = yourReview.findViewById(R.id.buttonAddReview)
        editReviewButton = yourReview.findViewById(R.id.buttonEditReview)
        saveReviewButton = yourReview.findViewById(R.id.buttonSaveReview)
        existingReview = yourReview.findViewById(R.id.existingReview)
        writeReview = yourReview.findViewById(R.id.writeReview)

        //Common initializations
        yourUsername.text = "johndoe" //viewModel.yourReview.value.username

        if ( //Case 1: no rate and no review
        //viewModel.yourReview.value.id == 0
        //&& viewModel.yourReview.value.qualityRating == 0
        //&& viewModel.yourReview.value.facilitiesRating == 0
        //&& viewModel.yourReview.value.text == ""
            viewModel.yourReview.value != null
        ) {
            yourReviewDate.visibility = TextView.GONE
            yourQualityRating.rating = 0f
            yourFacilitiesRating.rating = 0f
            yourReviewText.text = ""
            yourReviewEditText.setText("", TextView.BufferType.EDITABLE)
        } else if ( //Case 2: rate but not review
        //&& viewModel.yourReview.value.text == ""
            viewModel.yourReview.value != null
        ) {
            yourReviewDate.text =
                "6/5/23" //viewModel.yourReview.value?.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            yourReviewDate.visibility = TextView.VISIBLE
            yourQualityRating.rating = 3f //viewModel.yourReview.value?.qualityRating
            yourFacilitiesRating.rating = 3f //viewModel.yourReview.value?.facilitiesRating
            yourReviewText.text = ""
            yourReviewEditText.setText("", TextView.BufferType.EDITABLE)
        } else { //Case 3: rate and review
            yourReviewDate.text =
                "6/5/23" //viewModel.yourReview.value?.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            yourReviewDate.visibility = TextView.VISIBLE
            yourQualityRating.rating = 3f //viewModel.yourReview.value?.qualityRating
            yourFacilitiesRating.rating = 3f //viewModel.yourReview.value?.facilitiesRating
            yourReviewText.text = "Some text" //viewModel.yourReview.value?.text
            yourReviewEditText.setText(
                "Some text" /*viewModel.yourReview.value?.text*/,
                TextView.BufferType.EDITABLE
            )
            addReviewButton.visibility = Button.GONE
            existingReview.visibility = LinearLayout.VISIBLE
        }

        //setListeners
        yourQualityRating.setOnRatingBarChangeListener { _, fl, _ -> handleQualityRatingBar(fl) }
        yourFacilitiesRating.setOnRatingBarChangeListener { _, fl, _ -> handleFacilitiesRatingBar(fl) }
        addReviewButton.setOnClickListener { handleAddReviewButton() }
        editReviewButton.setOnClickListener { handleEditReviewButton() }
        saveReviewButton.setOnClickListener { handleSaveReviewButton() }

        yourReviewContainer.addView(yourReview)
    }

    private fun initReviewList() {
        reviewList.apply {
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = reviewAdapter
        }
        reviewAdapter.reviews.clear()
        //reviewAdapter.reviews.addAll(viewModel.playground.value?.reviews)
        reviewAdapter.notifyDataSetChanged()
    }

    private fun handleAddReservationButton() {
        //TODO: navigate to new reservation
    }

    private fun handleDirectionsButton() {
        val formattedAddress = playgroundAddress.text
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$formattedAddress")
        )
        startActivity(intent)
    }

    private fun chooseImage(view: ImageView) {
        when (viewModel.playground.value?.sportId) {
            //TODO n -> view.setImageDrawable(...)
        }
    }

    private fun handleQualityRatingBar(rating: Float) {
        //TODO
        //call viewModel and update db
    }

    private fun handleFacilitiesRatingBar(rating: Float) {
        //TODO
        //call viewModel and update db
    }

    private fun handleAddReviewButton() {
        addReviewButton.visibility = Button.GONE
        writeReview.visibility = LinearLayout.VISIBLE
    }

    private fun handleEditReviewButton() {
        existingReview.visibility = LinearLayout.GONE
        writeReview.visibility = LinearLayout.VISIBLE
    }

    private fun handleSaveReviewButton() {
        //TODO
        yourReviewText.text = yourReviewEditText.text
        writeReview.visibility = LinearLayout.GONE

        if (yourReviewText.text == "") {
            addReviewButton.visibility = Button.VISIBLE
        } else {
            existingReview.visibility = LinearLayout.VISIBLE
        }

        //TODO: call viewModel to store the new review
    }

}