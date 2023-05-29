package it.polito.mad.sportapp.playground_details

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.playground_details.reviews_recycler_view.ReviewAdapter

@AndroidEntryPoint
class PlaygroundDetailsFragment : Fragment(R.layout.fragment_playground_details) {

    internal val viewModel by viewModels<PlaygroundDetailsViewModel>()

    internal var playgroundId = ""
    internal var selectedSlotInPlaygroundAvailabilities: String? = null
    private var scrollToReview = false

    internal lateinit var scrollView: ScrollView
    internal lateinit var progressBar: View

    internal lateinit var playgroundImage: ImageView
    internal lateinit var overallRatingBar: RatingBar
    internal lateinit var playgroundName: TextView
    internal lateinit var sportCenterName: TextView
    internal lateinit var sportEmoji: TextView
    internal lateinit var playgroundSport: TextView
    internal lateinit var playgroundAddress: TextView
    internal lateinit var playgroundOpeningTime: TextView
    internal lateinit var playgroundClosingTime: TextView
    internal lateinit var playgroundPrice: TextView
    internal lateinit var addReservationButton: Button
    internal lateinit var directionsButton: Button

    internal lateinit var equipmentsSection: LinearLayout
    internal lateinit var equipments: LinearLayout

    internal lateinit var playgroundQualityRatingBar: RatingBar
    internal lateinit var playgroundFacilitiesRatingBar: RatingBar
    internal lateinit var noQualityRatingMessage: TextView
    internal lateinit var noFacilitiesRatingMessage: TextView

    internal lateinit var yourReviewContainer: LinearLayout
    internal lateinit var reviewList: RecyclerView
    internal val reviewAdapter = ReviewAdapter()

    internal lateinit var yourReview: View
    internal lateinit var youCanNotReviewMessage: TextView
    internal lateinit var yourReviewBody: LinearLayout
    internal lateinit var yourUsername: TextView
    internal lateinit var yourReviewDate: TextView
    internal lateinit var yourQualityRating: RatingBar
    internal lateinit var yourFacilitiesRating: RatingBar
    internal lateinit var yourReviewTitle: TextView
    internal lateinit var yourReviewText: TextView
    internal lateinit var yourReviewLastUpdateContainer: LinearLayout
    internal lateinit var yourReviewLastUpdate: TextView
    internal lateinit var yourReviewEditTitle: EditText
    internal lateinit var yourReviewEditText: EditText
    internal lateinit var addReviewButton: Button
    internal lateinit var editReviewButton: Button
    internal lateinit var deleteReviewButton: Button
    internal lateinit var saveReviewButton: Button
    internal lateinit var existingReview: LinearLayout
    internal lateinit var writeReview: LinearLayout

    // action bar
    internal var actionBar: ActionBar? = null

    private lateinit var bottomNavigationBar: View
    internal lateinit var navController: NavController

    private lateinit var fireListener: FireListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get activity action bar
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        bottomNavigationBar =
            (requireActivity() as AppCompatActivity).findViewById(R.id.bottom_navigation_bar)

        bottomNavigationBar.visibility = View.GONE

        // initialize menu
        menuInit()

        // initialize navigation controller
        navController = Navigation.findNavController(view)

        // Retrieve playground id
        //playgroundId = arguments?.getString("id_playground") ?: ""
        //TODO: remove this hardcoded assignment
        playgroundId = "v3MXUPjN8F5t8WN4F7qr"
        selectedSlotInPlaygroundAvailabilities = arguments?.getString("selected_slot")
        scrollToReview = arguments?.getBoolean("scroll_to_review") ?: false

        if(playgroundId != ""){
            fireListener = viewModel.getPlaygroundFromDb(playgroundId)
        }

        // Retrieve views
        retrieveViews()
        scrollView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        viewModel.playground.observe(viewLifecycleOwner) {
            if (viewModel.playground.value != null) {
                if(viewModel.equipments.value == null) {
                    viewModel.loadEquipmentsFromDb()
                }
                viewModel.setYourReview()
                initViews()
                initYourReview()
                initReviewList()

                progressBar.visibility = View.GONE
                scrollView.visibility = View.VISIBLE

                if(scrollToReview){
                    //if this page was called from the "Leave a review" button, it will
                    //immediately scroll to the review form
                    scrollToReview = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        scrollView.scrollToDescendant(yourReview)
                    } else {
                        scrollView.scrollTo(0, 10)
                    }
                }
            }
        }

        viewModel.yourReview.observe(viewLifecycleOwner) {
            if(viewModel.yourReview.value?.id != null){
                initYourReview()
            }
        }

        viewModel.equipments.observe(viewLifecycleOwner) {
            viewModel.equipments.value?.let {
                initEquipments()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fireListener.unregister()
    }

}