package it.polito.mad.sportapp.playground_details

import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R

internal fun PlaygroundDetailsFragment.initYourReview() {
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
        (viewModel.yourReview.value?.id  == 0)
        && (viewModel.yourReview.value?.qualityRating == 0f)
        && (viewModel.yourReview.value?.facilitiesRating == 0f)
        && (viewModel.yourReview.value?.review == "")
    ) {
        yourReviewDate.visibility = TextView.GONE
        yourQualityRating.rating = 0f
        yourFacilitiesRating.rating = 0f
        yourReviewText.text = ""
        yourReviewEditText.setText("", TextView.BufferType.EDITABLE)
    } else if ( //Case 2: rate but not review
        viewModel.yourReview.value?.review == ""
    ) {
        yourReviewDate.text =
            "6/5/23" //viewModel.yourReview.value?.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        yourReviewDate.visibility = TextView.VISIBLE
        yourQualityRating.rating = viewModel.yourReview.value?.qualityRating!!
        yourFacilitiesRating.rating = viewModel.yourReview.value?.facilitiesRating!!
        yourReviewText.text = ""
        yourReviewEditText.setText("", TextView.BufferType.EDITABLE)
    } else { //Case 3: rate and review
        yourReviewDate.text =
            "6/5/23" //viewModel.yourReview.value?.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        yourReviewDate.visibility = TextView.VISIBLE
        yourQualityRating.rating = viewModel.yourReview.value?.qualityRating!!
        yourFacilitiesRating.rating = viewModel.yourReview.value?.facilitiesRating!!
        yourReviewText.text = viewModel.yourReview.value?.review!!
        yourReviewEditText.setText(
            viewModel.yourReview.value?.review,
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

internal fun PlaygroundDetailsFragment.initReviewList() {
    reviewList.apply {
        layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = reviewAdapter
    }
    reviewAdapter.reviews.clear()
    //reviewAdapter.reviews.addAll(viewModel.playground.value?.reviews)
    reviewAdapter.notifyDataSetChanged()
}

internal fun PlaygroundDetailsFragment.handleQualityRatingBar(rating: Float) {
    viewModel.updateQualityRating(rating)
}

internal fun PlaygroundDetailsFragment.handleFacilitiesRatingBar(rating: Float) {
    viewModel.updateFacilitiesRating(rating)
}

internal fun PlaygroundDetailsFragment.handleAddReviewButton() {
    addReviewButton.visibility = Button.GONE
    writeReview.visibility = LinearLayout.VISIBLE
}

internal fun PlaygroundDetailsFragment.handleEditReviewButton() {
    existingReview.visibility = LinearLayout.GONE
    writeReview.visibility = LinearLayout.VISIBLE
}

internal fun PlaygroundDetailsFragment.handleSaveReviewButton() {

    yourReviewText.text = yourReviewEditText.text
    writeReview.visibility = LinearLayout.GONE

    if (yourReviewText.text == "") {
        addReviewButton.visibility = Button.VISIBLE
    } else {
        existingReview.visibility = LinearLayout.VISIBLE
    }

    //TODO: call viewModel to store the new review
}