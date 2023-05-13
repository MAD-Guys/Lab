package it.polito.mad.sportapp.playground_details

import android.annotation.SuppressLint
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.showToasty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal fun PlaygroundDetailsFragment.initYourReview() {
    yourReviewContainer.removeAllViewsInLayout()
    yourReview = layoutInflater.inflate(R.layout.your_review, yourReviewContainer, false)

    //retrieve views
    yourUsername = yourReview.findViewById(R.id.username)
    yourReviewDate = yourReview.findViewById(R.id.date)
    yourQualityRating = yourReview.findViewById(R.id.qualityRatingBar)
    yourFacilitiesRating = yourReview.findViewById(R.id.facilitiesRatingBar)
    yourReviewTitle = yourReview.findViewById(R.id.reviewTitle)
    yourReviewText = yourReview.findViewById(R.id.reviewBody)
    yourReviewLastUpdateContainer = yourReview.findViewById(R.id.lastUpdate_container)
    yourReviewLastUpdate = yourReview.findViewById(R.id.lastUpdate)
    yourReviewEditTitle = yourReview.findViewById(R.id.reviewInputTitle)
    yourReviewEditText = yourReview.findViewById(R.id.reviewInputBody)
    addReviewButton = yourReview.findViewById(R.id.buttonAddReview)
    editReviewButton = yourReview.findViewById(R.id.buttonEditReview)
    deleteReviewButton = yourReview.findViewById(R.id.buttonDeleteReview)
    saveReviewButton = yourReview.findViewById(R.id.buttonSaveReview)
    existingReview = yourReview.findViewById(R.id.existingReview)
    writeReview = yourReview.findViewById(R.id.writeReview)

    //Common initializations
    if(viewModel.yourReview.value?.username.isNullOrBlank())
        yourUsername.text = "johndoe"
    else
        yourUsername.text = viewModel.yourReview.value?.username

    if( //Case 0: Edit mode
        viewModel.isEditMode()
    ){
        yourReviewDate.text = viewModel.yourReview.value?.publicationDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        yourReviewDate.visibility = TextView.VISIBLE
        yourQualityRating.rating = viewModel.yourReview.value?.qualityRating!!
        yourFacilitiesRating.rating = viewModel.yourReview.value?.facilitiesRating!!
        yourReviewTitle.text = viewModel.yourReview.value?.title
        yourReviewText.text = viewModel.yourReview.value?.review!!
        yourReviewEditTitle.setText(
            viewModel.getTempTitle(),
            TextView.BufferType.EDITABLE
        )
        yourReviewEditText.setText(
            viewModel.getTempText(),
            TextView.BufferType.EDITABLE
        )
        yourReviewLastUpdate.text = viewModel.yourReview.value?.lastUpdateDate?.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        if(viewModel.yourReview.value?.lastUpdateDate?.isEqual(viewModel.yourReview.value?.publicationDate) == true){
            yourReviewLastUpdateContainer.visibility = LinearLayout.GONE
        }
        addReviewButton.visibility = Button.GONE
        existingReview .visibility = LinearLayout.GONE
        writeReview.visibility = LinearLayout.VISIBLE

    } else if ( //Case 1: no rate and no review
        (viewModel.yourReview.value?.id  == 0)
        && (viewModel.yourReview.value?.qualityRating == 0f)
        && (viewModel.yourReview.value?.facilitiesRating == 0f)
        && (viewModel.yourReview.value?.title == "")
        && (viewModel.yourReview.value?.review == "")
    ) {
        yourReviewDate.visibility = TextView.GONE
        yourQualityRating.rating = 0f
        yourFacilitiesRating.rating = 0f
        yourReviewTitle.text = ""
        yourReviewText.text = ""
        yourReviewEditTitle.setText("", TextView.BufferType.EDITABLE)
        yourReviewEditText.setText("", TextView.BufferType.EDITABLE)
    } else if ( //Case 2: rate but not review
        (viewModel.yourReview.value?.title == "")
        && (viewModel.yourReview.value?.review == "")
    ) {
        yourReviewDate.text = viewModel.yourReview.value?.publicationDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        yourReviewDate.visibility = TextView.VISIBLE
        yourQualityRating.rating = viewModel.yourReview.value?.qualityRating!!
        yourFacilitiesRating.rating = viewModel.yourReview.value?.facilitiesRating!!
        yourReviewTitle.text = ""
        yourReviewText.text = ""
        yourReviewEditTitle.setText("", TextView.BufferType.EDITABLE)
        yourReviewEditText.setText("", TextView.BufferType.EDITABLE)
    } else { //Case 3: rate and review
        yourReviewDate.text = viewModel.yourReview.value?.publicationDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        yourReviewDate.visibility = TextView.VISIBLE
        yourQualityRating.rating = viewModel.yourReview.value?.qualityRating!!
        yourFacilitiesRating.rating = viewModel.yourReview.value?.facilitiesRating!!
        yourReviewTitle.text = viewModel.yourReview.value?.title
        yourReviewText.text = viewModel.yourReview.value?.review!!
        yourReviewEditTitle.setText(
            viewModel.yourReview.value?.title,
            TextView.BufferType.EDITABLE
        )
        yourReviewEditText.setText(
            viewModel.yourReview.value?.review,
            TextView.BufferType.EDITABLE
        )
        yourReviewLastUpdate.text = viewModel.yourReview.value?.lastUpdateDate?.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        if(viewModel.yourReview.value?.lastUpdateDate?.isEqual(viewModel.yourReview.value?.publicationDate) == true){
            yourReviewLastUpdateContainer.visibility = LinearLayout.GONE
        }
        addReviewButton.visibility = Button.GONE
        existingReview.visibility = LinearLayout.VISIBLE
    }

    //setListeners
    yourQualityRating.setOnRatingBarChangeListener { _, fl, _ -> handleQualityRatingBar(fl) }
    yourFacilitiesRating.setOnRatingBarChangeListener { _, fl, _ -> handleFacilitiesRatingBar(fl) }
    addReviewButton.setOnClickListener { handleAddReviewButton() }
    editReviewButton.setOnClickListener { handleEditReviewButton() }
    deleteReviewButton.setOnClickListener { handleDeleteReviewButton() }
    saveReviewButton.setOnClickListener { handleSaveReviewButton() }

    yourReviewContainer.addView(yourReview)
}

@SuppressLint("NotifyDataSetChanged")
internal fun PlaygroundDetailsFragment.initReviewList() {
    reviewList.apply {
        layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = reviewAdapter
    }
    reviewAdapter.reviews.clear()
    viewModel.playground.value?.reviewList?.let {
        reviewAdapter.reviews.addAll(it.filter { r -> r.userId != 1 }) //TODO: replace 1 with the id of the logged user
    }
    reviewAdapter.notifyDataSetChanged()
}

internal fun PlaygroundDetailsFragment.handleQualityRatingBar(rating: Float) {
    viewModel.updateQualityRating(rating)
    showToasty("success", requireContext(), "New quality rating saved!")

    //if the user is editing, save the current status
    if(viewModel.isEditMode()){
        viewModel.saveEditStatus(yourReviewEditTitle.text.toString(), yourReviewEditText.text.toString())
    }

    // reload the playground with the new rating
    viewModel.clearPlayground()
    viewModel.getPlaygroundFromDb(viewModel.playground.value!!.playgroundId)
}

internal fun PlaygroundDetailsFragment.handleFacilitiesRatingBar(rating: Float) {
    viewModel.updateFacilitiesRating(rating)
    showToasty("success", requireContext(), "New facilities rating saved!")

    //if the user is editing, save the current status
    if(viewModel.isEditMode()){
        viewModel.saveEditStatus(yourReviewEditTitle.text.toString(), yourReviewEditText.text.toString())
    }

    // reload the playground with the new rating
    viewModel.clearPlayground()
    viewModel.getPlaygroundFromDb(viewModel.playground.value!!.playgroundId)
}

internal fun PlaygroundDetailsFragment.handleAddReviewButton() {
    addReviewButton.visibility = Button.GONE
    writeReview.visibility = LinearLayout.VISIBLE
    viewModel.setEditMode(true)
}

internal fun PlaygroundDetailsFragment.handleEditReviewButton() {
    existingReview.visibility = LinearLayout.GONE
    writeReview.visibility = LinearLayout.VISIBLE
    viewModel.setEditMode(true)
}

internal fun PlaygroundDetailsFragment.handleDeleteReviewButton() {

    AlertDialog.Builder(requireContext())
        .setMessage("Are you sure to delete this review?")
        .setPositiveButton("YES") { _, _ ->
            // delete the review
            viewModel.deleteReview()

            showToasty(
                "success",
                requireContext(),
                "Review correctly deleted"
            )

            // reload the playground with the new rating
            viewModel.clearPlayground()
            viewModel.getPlaygroundFromDb(viewModel.playground.value!!.playgroundId)
        }
        .setNegativeButton("NO") { d, _ -> d.cancel() }
        .create()
        .show()
}

internal fun PlaygroundDetailsFragment.handleSaveReviewButton() {

    yourReviewTitle.text = yourReviewEditTitle.text
    yourReviewText.text = yourReviewEditText.text
    yourReviewLastUpdate.text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    writeReview.visibility = LinearLayout.GONE

    if (yourReviewTitle.text.isBlank() && yourReviewText.text.isBlank()) {
        addReviewButton.visibility = Button.VISIBLE
    } else {
        if(viewModel.yourReview.value?.lastUpdateDate?.isEqual(viewModel.yourReview.value?.publicationDate) == true){
            yourReviewLastUpdateContainer.visibility = LinearLayout.GONE
        } else {
            yourReviewLastUpdateContainer.visibility = LinearLayout.VISIBLE
        }
        existingReview.visibility = LinearLayout.VISIBLE
    }

    viewModel.updateReview(
        yourQualityRating.rating,
        yourFacilitiesRating.rating,
        yourReviewTitle.text.toString(),
        yourReviewText.text.toString()
    )

    viewModel.setEditMode(false)

    showToasty("success", requireContext(), "Review correctly saved!")
}